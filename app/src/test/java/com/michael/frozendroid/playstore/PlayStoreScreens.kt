package com.michael.frozendroid.playstore

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FrozenPrimary = Color(0xFF1F6FEB)
private val FrozenTeal = Color(0xFF00A6A6)
private val FrozenIce = Color(0xFFEAF6FF)
private val FrozenInk = Color(0xFF102033)

private val PlayStoreColorScheme = lightColorScheme(
  primary = FrozenPrimary,
  secondary = FrozenTeal,
  tertiary = Color(0xFF7A4FBE),
  background = Color(0xFFF7FAFC),
  surface = Color.White,
  surfaceVariant = Color(0xFFE8EEF5),
  onPrimary = Color.White,
  onBackground = FrozenInk,
  onSurface = FrozenInk,
)

enum class PlayStoreScene {
  Dashboard,
  Directory,
  Profiles,
  Telemetry,
}

private data class DemoApp(
  val label: String,
  val packageName: String,
  val status: String,
  val level: String,
  val description: String,
)

private val demoApps = listOf(
  DemoApp("Carrier Hub", "com.carrier.services", "Frozen", "Safe", "Background package held from waking the device."),
  DemoApp("OEM Analytics", "com.oem.analytics", "Ready", "Caution", "Known telemetry component with manufacturer-specific behavior."),
  DemoApp("Trial Games", "com.vendor.games.trial", "Frozen", "Safe", "Preinstalled promotion package with no core dependency."),
  DemoApp("System UI", "com.android.systemui", "Protected", "Danger", "Essential Android component blocked from freeze actions."),
)

@Composable
fun PlayStoreScreenshotFrame(scene: PlayStoreScene) {
  MaterialTheme(colorScheme = PlayStoreColorScheme) {
    Scaffold(
      topBar = { PlayStoreTopBar(scene) },
      bottomBar = { PlayStoreBottomBar(scene) },
      containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
      when (scene) {
        PlayStoreScene.Dashboard -> DashboardScene(Modifier.padding(padding))
        PlayStoreScene.Directory -> DirectoryScene(Modifier.padding(padding))
        PlayStoreScene.Profiles -> ProfilesScene(Modifier.padding(padding))
        PlayStoreScene.Telemetry -> TelemetryScene(Modifier.padding(padding))
      }
    }
  }
}

@Composable
fun PlayStoreFeatureGraphic() {
  MaterialTheme(colorScheme = PlayStoreColorScheme) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.horizontalGradient(
            listOf(Color(0xFF102033), Color(0xFF1F6FEB), Color(0xFF00A6A6)),
          ),
        ),
    ) {
      Column(
        modifier = Modifier
          .align(Alignment.CenterStart)
          .padding(start = 72.dp)
          .width(430.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        Text("FrozenDroid", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Black)
        Text(
          "Freeze bloatware, protect essentials, and spot battery wakeups.",
          color = FrozenIce,
          fontSize = 24.sp,
          lineHeight = 31.sp,
          fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          FeaturePill("Shizuku-powered")
          FeaturePill("Offline safety guide")
        }
      }

      Box(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(end = 78.dp)
          .width(250.dp)
          .height(440.dp)
          .clip(RoundedCornerShape(32.dp))
          .background(Color(0xFFEEF7FF))
          .padding(14.dp),
      ) {
        MiniPhonePreview()
      }
    }
  }
}

@Composable
private fun PlayStoreTopBar(scene: PlayStoreScene) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(horizontal = 22.dp, vertical = 18.dp),
  ) {
    Text(
      text = when (scene) {
        PlayStoreScene.Dashboard -> "FrozenDroid"
        PlayStoreScene.Directory -> "Offline Directory"
        PlayStoreScene.Profiles -> "Freeze Presets"
        PlayStoreScene.Telemetry -> "Power Telemetry"
      },
      fontSize = 28.sp,
      fontWeight = FontWeight.Black,
      color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
      text = when (scene) {
        PlayStoreScene.Dashboard -> "Installed packages with safety-aware freeze actions"
        PlayStoreScene.Directory -> "Manufacturer package guidance with local overrides"
        PlayStoreScene.Profiles -> "Reusable package groups for quick switching"
        PlayStoreScene.Telemetry -> "CPU wakeup patterns and top offenders"
      },
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun DashboardScene(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    SearchField()
    SummaryRow()
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(bottom = 12.dp),
    ) {
      items(demoApps) { app -> AppRow(app) }
    }
  }
}

@Composable
private fun DirectoryScene(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf("Universal", "Samsung", "Xiaomi").forEachIndexed { index, label ->
        FilterChip(selected = index == 1, onClick = {}, label = { Text(label, fontSize = 12.sp) })
      }
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      items(demoApps.reversed()) { app -> DirectoryRow(app) }
    }
  }
}

@Composable
private fun ProfilesScene(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text("Choose preset to activate", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
    ProfileCard("Commute Mode", "8 packages frozen", true, Icons.Default.Build)
    ProfileCard("Work Focus", "12 packages frozen", false, Icons.Default.Lock)
    Card(
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(18.dp),
    ) {
      Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Active profile details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Carrier Hub, Trial Games, OEM Analytics and 5 more stay frozen until the preset is changed.", fontSize = 13.sp)
      }
    }
  }
}

@Composable
private fun TelemetryScene(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf("Last 8h", "24 Hours", "7 Days").forEachIndexed { index, label ->
        FilterChip(selected = index == 1, onClick = {}, label = { Text(label, fontSize = 12.sp) })
      }
    }
    WakeChart()
    Card(
      colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E4)),
      shape = RoundedCornerShape(18.dp),
    ) {
      Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB55D00))
        Column {
          Text("Top offender detected", fontWeight = FontWeight.Bold)
          Text("com.oem.analytics triggered 148 wakeups in 24 hours.", fontSize = 13.sp)
        }
      }
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      items(listOf("148 wakeups - OEM Analytics", "72 wakeups - Carrier Hub", "39 wakeups - Trial Games")) {
        Card(shape = RoundedCornerShape(14.dp)) {
          Text(it, modifier = Modifier.padding(14.dp), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
private fun SearchField() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(horizontal = 16.dp, vertical = 13.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Icon(Icons.Default.Search, contentDescription = null, tint = FrozenPrimary)
    Text("Search installed packages...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f))
  }
}

@Composable
private fun SummaryRow() {
  Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
    StatTile("17", "Safe", Color(0xFFE5F7EF), Color(0xFF167346), Modifier.weight(1f))
    StatTile("6", "Caution", Color(0xFFFFF4E4), Color(0xFFB55D00), Modifier.weight(1f))
    StatTile("4", "Frozen", Color(0xFFEAF0FF), FrozenPrimary, Modifier.weight(1f))
  }
}

@Composable
private fun StatTile(value: String, label: String, color: Color, textColor: Color, modifier: Modifier = Modifier) {
  Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color), shape = RoundedCornerShape(16.dp)) {
    Column(Modifier.padding(14.dp)) {
      Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = textColor)
      Text(label, fontSize = 12.sp, color = textColor)
    }
  }
}

@Composable
private fun AppRow(app: DemoApp) {
  Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
    Row(
      Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      StatusDot(app.status)
      Column(Modifier.weight(1f)) {
        Text(app.label, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(app.packageName, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(app.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 2, overflow = TextOverflow.Ellipsis)
      }
      SafetyBadge(app.level)
    }
  }
}

@Composable
private fun DirectoryRow(app: DemoApp) {
  AppRow(app)
}

@Composable
private fun ProfileCard(title: String, subtitle: String, active: Boolean, icon: ImageVector) {
  Card(
    colors = CardDefaults.cardColors(containerColor = if (active) Color(0xFFDDEBFF) else MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(22.dp),
  ) {
    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      Box(Modifier.size(52.dp).clip(CircleShape).background(FrozenPrimary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = FrozenPrimary)
      }
      Column(Modifier.weight(1f)) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
      }
      if (active) SafetyBadge("Active")
    }
  }
}

@Composable
private fun WakeChart() {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .height(230.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(20.dp),
  ) {
    Box(Modifier.fillMaxSize().padding(20.dp)) {
      Canvas(Modifier.fillMaxSize()) {
        val values = listOf(28f, 62f, 44f, 91f, 70f, 148f, 116f)
        val max = values.max()
        val step = size.width / (values.lastIndex.coerceAtLeast(1))
        val path = Path()
        values.forEachIndexed { index, value ->
          val x = index * step
          val y = size.height - (value / max * size.height * 0.82f) - 18f
          if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
          drawCircle(FrozenTeal, 7f, Offset(x, y))
        }
        for (i in 1..3) {
          val y = size.height * i / 4f
          drawLine(Color.Gray.copy(alpha = 0.16f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
        drawPath(path, FrozenPrimary, style = Stroke(width = 5f))
      }
      Text("CPU wakeups profile", modifier = Modifier.align(Alignment.BottomStart), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FrozenPrimary)
    }
  }
}

@Composable
private fun PlayStoreBottomBar(scene: PlayStoreScene) {
  NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
    NavigationBarItem(selected = scene == PlayStoreScene.Dashboard, onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
    NavigationBarItem(selected = scene == PlayStoreScene.Profiles, onClick = {}, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Profiles") })
    NavigationBarItem(selected = scene == PlayStoreScene.Telemetry, onClick = {}, icon = { Icon(Icons.Default.Info, null) }, label = { Text("CPU") })
    NavigationBarItem(selected = scene == PlayStoreScene.Directory, onClick = {}, icon = { Icon(Icons.Default.List, null) }, label = { Text("Directory") })
  }
}

@Composable
private fun StatusDot(status: String) {
  val color = when (status) {
    "Frozen" -> FrozenPrimary
    "Protected" -> Color(0xFFC62828)
    else -> FrozenTeal
  }
  Box(Modifier.size(42.dp).clip(CircleShape).background(color.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
    Icon(if (status == "Frozen") Icons.Default.CheckCircle else Icons.Default.Add, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
  }
}

@Composable
private fun SafetyBadge(label: String) {
  val color = when (label) {
    "Safe", "Active" -> Color(0xFF167346)
    "Caution" -> Color(0xFFB55D00)
    "Danger" -> Color(0xFFC62828)
    else -> FrozenPrimary
  }
  Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)) {
    Text(label.uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
  }
}

@Composable
private fun FeaturePill(text: String) {
  Surface(color = Color.White.copy(alpha = 0.16f), shape = RoundedCornerShape(18.dp)) {
    Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
  }
}

@Composable
private fun MiniPhonePreview() {
  Column(
    Modifier
      .fillMaxSize()
      .clip(RoundedCornerShape(22.dp))
      .background(Color.White)
      .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text("FrozenDroid", fontWeight = FontWeight.Black, fontSize = 20.sp)
    SearchField()
    SummaryRow()
    demoApps.take(3).forEach { AppRow(it) }
  }
}
