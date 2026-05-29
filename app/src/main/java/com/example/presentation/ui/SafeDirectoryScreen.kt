package com.example.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppPackage
import com.example.domain.model.SafetyLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeDirectoryScreen(
    viewModel: SafeDirectoryViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToCpu: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val carrierFilter by viewModel.carrierFilter.collectAsState()
    var selectedAppForOverride by remember { mutableStateOf<AppPackage?>(null) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Offline Directory", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "directory",
                onNavigateToHome = onNavigateToHome,
                onNavigateToProfiles = onNavigateToProfiles,
                onNavigateToCpu = onNavigateToCpu,
                onNavigateToSafeDirectory = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Manufacturer Horizontal Selector
            ManufacturerFilterRow(
                selectedCarrier = carrierFilter,
                onSelected = { viewModel.setCarrierFilter(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (uiState) {
                is SafeDirectoryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SafeDirectoryUiState.Success -> {
                    val state = uiState as SafeDirectoryUiState.Success
                    val apps = state.apps
                    val overrides = state.overrides

                    if (apps.isEmpty()) {
                        EmptyStatePlaceholder("No catalog bloatware data mapped.")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(apps, key = { it.packageName }) { app ->
                                DirectoryAppItem(
                                    app = app,
                                    currentOverride = overrides[app.packageName],
                                    onEditOverride = {
                                        selectedAppForOverride = app
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Override Choice dialog
    if (selectedAppForOverride != null) {
        val app = selectedAppForOverride!!
        OverrideDialog(
            app = app,
            onDismiss = { selectedAppForOverride = null },
            onConfirm = { chosenLevel ->
                viewModel.saveOverride(app.packageName, chosenLevel)
                selectedAppForOverride = null
            }
        )
    }
}

@Composable
fun ManufacturerFilterRow(
    selectedCarrier: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val labels = listOf("Universal", "Samsung", "Xiaomi", "Oppo", "Google")
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(labels) { label ->
            val isSelected = selectedCarrier.equals(label, ignoreCase = true)
            InputChip(
                selected = isSelected,
                onClick = { onSelected(label) },
                label = { Text(label, fontSize = 12.sp) }
            )
        }
    }
}

@Composable
fun DirectoryAppItem(
    app: AppPackage,
    currentOverride: SafetyLevel?,
    onEditOverride: () -> Unit
) {
    val safetyToDisplay = currentOverride ?: app.safetyLevel
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        app.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        app.packageName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SafetyBadge(safetyToDisplay)
                    if (currentOverride != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "OVERRIDER",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    IconButton(onClick = onEditOverride) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit override",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (app.description.isEmpty()) "Standard platform component of manufacturer packages." else app.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OverrideDialog(
    app: AppPackage,
    onDismiss: () -> Unit,
    onConfirm: (SafetyLevel?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Safety Exclusion Override") },
        text = {
            Column {
                Text(
                    text = "Package: ${app.packageName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Override the pre-defined safety rank. Setting a package to SAFE permits immediate freezing. WARNING: setting essential components to SAFE might trigger bootloops.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // Override Option Buttons
                Button(
                    onClick = { onConfirm(SafetyLevel.SAFE) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Deem SAFE to freeze", color = Color.White)
                }

                Button(
                    onClick = { onConfirm(SafetyLevel.CAUTION) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Deem CAUTION level", color = Color.White)
                }

                Button(
                    onClick = { onConfirm(SafetyLevel.DANGEROUS) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Deem DANGEROUS (Freeze locked)", color = Color.White)
                }

                OutlinedButton(
                    onClick = { onConfirm(null) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Reset to default Catalog score")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
