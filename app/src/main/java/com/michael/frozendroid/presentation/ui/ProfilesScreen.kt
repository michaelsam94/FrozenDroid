package com.michael.frozendroid.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.michael.frozendroid.domain.model.FreezeProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    viewModel: ProfilesViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToCpu: () -> Unit,
    onNavigateToSafeDirectory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedProfileForSwitch by remember { mutableStateOf<FreezeProfile?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = { Text("Freeze Presets", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.testTag("create_profile_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Profile")
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "profiles",
                onNavigateToHome = onNavigateToHome,
                onNavigateToProfiles = {},
                onNavigateToCpu = onNavigateToCpu,
                onNavigateToSafeDirectory = onNavigateToSafeDirectory
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is ProfilesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProfilesUiState.Success -> {
                    val state = uiState as ProfilesUiState.Success
                    val profiles = state.profiles
                    val activeProfile = state.activeProfile

                    if (profiles.isEmpty()) {
                        EmptyStatePlaceholder("No presets available. Build one!")
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = "Choose Profile preset to activate:",
                                modifier = Modifier.padding(horizontal = 24.dp),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Horizontal Card Roll
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(profiles) { profile ->
                                    val isActive = activeProfile?.id == profile.id
                                    ProfileSlideCard(
                                        profile = profile,
                                        isActive = isActive,
                                        onClick = {
                                            selectedProfileForSwitch = profile
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Details block
                            if (activeProfile != null) {
                                ActiveProfileDetailView(activeProfile, onDelete = {
                                    viewModel.deleteProfile(it.id)
                                    scope.launch { snackbarHostState.showSnackbar("Deleted preset ${it.name}") }
                                })
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(24.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No profile currently activated.\nAll packages are operating normally.",
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue to input creation
    if (showCreateDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, packages ->
                viewModel.createProfile(name, packages)
                showCreateDialog = false
            }
        )
    }

    // Switch Profile Bottom Sheet
    if (selectedProfileForSwitch != null) {
        val target = selectedProfileForSwitch!!
        ModalBottomSheet(
            onDismissRequest = { selectedProfileForSwitch = null }
        ) {
            SwitchProfileConfirmationContent(
                profile = target,
                onDismiss = { selectedProfileForSwitch = null },
                onConfirm = {
                    viewModel.switchProfile(target.id) { success ->
                        scope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar("Activated: ${target.name}")
                            } else {
                                snackbarHostState.showSnackbar("Activation failed")
                            }
                        }
                    }
                    selectedProfileForSwitch = null
                }
            )
        }
    }
}

@Composable
fun ProfileSlideCard(
    profile: FreezeProfile,
    isActive: Boolean,
    onClick: () -> Unit
) {
    // Pulse animation for active card
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Pulse circle glow in background
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha * 0.15f),
                            CircleShape
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (profile.icon == "bolt") Icons.Default.Build else Icons.Outlined.AccountBox,
                            contentDescription = null,
                            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isActive) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "ACTIVE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = profile.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${profile.frozenPackages.size} packages frozen",
                        fontSize = 12.sp,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveProfileDetailView(profile: FreezeProfile, onDelete: (FreezeProfile) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Preset Packages", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = { onDelete(profile) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete profile", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            profile.frozenPackages.forEach { pkg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(pkg, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SwitchProfileConfirmationContent(
    profile: FreezeProfile,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text("Switch Preset?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Activating '${profile.name}' will automatically suspend its registered ${profile.frozenPackages.size} background processes and revive all others.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp)) {
                Text("Cancel")
            }
            Button(onClick = onConfirm, modifier = Modifier.weight(1f).height(48.dp)) {
                Text("Confirm Switch")
            }
        }
    }
}

@Composable
fun CreateProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rawPackages by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Custom Preset") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = rawPackages,
                    onValueChange = { rawPackages = it },
                    label = { Text("Packages (comma-separated)") },
                    placeholder = { Text("e.g. com.facebook.system, com.miui.analytics") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val list = rawPackages.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    onConfirm(name.ifEmpty { "My Preset" }, list)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save Preset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
