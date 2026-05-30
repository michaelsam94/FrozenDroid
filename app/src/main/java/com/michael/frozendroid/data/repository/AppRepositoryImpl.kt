package com.michael.frozendroid.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.michael.frozendroid.data.local.daos.AppPackageDao
import com.michael.frozendroid.data.local.entities.AppPackageEntity
import com.michael.frozendroid.data.local.entities.UserOverrideEntity
import com.michael.frozendroid.domain.model.AppPackage
import com.michael.frozendroid.domain.model.SafetyLevel
import com.michael.frozendroid.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val context: Context,
    private val appPackageDao: AppPackageDao
) : AppRepository {

    // Cache of static catalog values loaded once
    private var staticCatalogMap: Map<String, CatalogInfo> = emptyMap()

    private data class CatalogInfo(
        val appName: String,
        val carrier: String,
        val safetyLevel: SafetyLevel,
        val reason: String
    )

    init {
        // Load bloatware catalog on background thread initially
        try {
            loadStaticCatalog()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadStaticCatalog() {
        val jsonString = context.assets.open("bloatware_db.json").bufferedReader().use { it.readText() }
        val array = JSONArray(jsonString)
        val tempMap = mutableMapOf<String, CatalogInfo>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val pkg = obj.getString("packageName")
            val name = obj.optString("appName", "")
            val carrier = obj.optString("carrier", "Universal")
            val safetyStr = obj.optString("safetyLevel", "SAFE")
            val lvl = try {
                SafetyLevel.valueOf(safetyStr)
            } catch (e: Exception) {
                SafetyLevel.SAFE
            }
            val reason = obj.optString("reason", "")
            tempMap[pkg] = CatalogInfo(name, carrier, lvl, reason)
        }
        staticCatalogMap = tempMap
    }

    override fun getApps(): Flow<List<AppPackage>> {
        val dbAppsFlow = appPackageDao.getAllAppsFlow()
        val overridesFlow = appPackageDao.getAllOverridesFlow()

        return combine(dbAppsFlow, overridesFlow) { dbApps, overrides ->
            val overrideMap = overrides.associate { it.packageName to SafetyLevel.valueOf(it.customSafetyLevel) }

            dbApps.map { entity ->
                val baseSafety = staticCatalogMap[entity.packageName]?.safetyLevel
                    ?: try { SafetyLevel.valueOf(entity.safetyLevel) } catch (e: Exception) { SafetyLevel.SAFE }
                val finalSafety = overrideMap[entity.packageName] ?: baseSafety
                val reason = staticCatalogMap[entity.packageName]?.reason ?: ""

                AppPackage(
                    packageName = entity.packageName,
                    label = entity.label,
                    icon = entity.icon,
                    isFrozen = entity.isFrozen,
                    safetyLevel = finalSafety,
                    carrier = entity.carrier,
                    description = reason
                )
            }
        }.flowOn(Dispatchers.Default)
    }

    override suspend fun refreshAppList() = withContext(Dispatchers.IO) {
        if (staticCatalogMap.isEmpty()) {
            loadStaticCatalog()
        }

        val pm = context.packageManager
        val flags = PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.MATCH_UNINSTALLED_PACKAGES
        val packages = pm.getInstalledPackages(flags) ?: emptyList()

        val appEntities = packages.mapNotNull { pkgInfo ->
            try {
                val pkgName = pkgInfo.packageName ?: return@mapNotNull null
                val appInfo = pkgInfo.applicationInfo ?: return@mapNotNull null

                val label = try {
                    appInfo.loadLabel(pm).toString()
                } catch (e: Exception) {
                    pkgName
                }
                
                val isFrozen = !appInfo.enabled

                // Default security profile mapping
                val catalogMatch = staticCatalogMap[pkgName]
                val defaultLvl = if (catalogMatch != null) {
                    catalogMatch.safetyLevel
                } else {
                    // Infer safety level from standard flags (system is CAUTION by default, launcher and critical is DANGEROUS)
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    if (isSystem) SafetyLevel.CAUTION else SafetyLevel.SAFE
                }

                AppPackageEntity(
                    packageName = pkgName,
                    label = label,
                    icon = null, // Store package name reference, load icon dynamically on UI thread 
                    isFrozen = isFrozen,
                    safetyLevel = defaultLvl.name,
                    carrier = catalogMatch?.carrier ?: "Universal"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        appPackageDao.clearAllApps()
        appPackageDao.insertApps(appEntities)
    }

    override suspend fun saveSafetyOverride(packageName: String, safetyLevel: SafetyLevel?) = withContext(Dispatchers.IO) {
        if (safetyLevel == null) {
            appPackageDao.deleteOverride(packageName)
        } else {
            appPackageDao.insertOverride(UserOverrideEntity(packageName, safetyLevel.name))
        }
    }

    override fun getSafetyOverrides(): Flow<Map<String, SafetyLevel>> {
        return appPackageDao.getAllOverridesFlow().map { list ->
            list.associate { it.packageName to SafetyLevel.valueOf(it.customSafetyLevel) }
        }.flowOn(Dispatchers.Default)
    }
}
