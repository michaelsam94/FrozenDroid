package com.example.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppPackage
import com.example.domain.model.SafetyLevel
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToProfiles: () -> Unit,
    onNavigateToCpu: () -> Unit,
    onNavigateToSafeDirectory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val selectedPackages by viewModel.selectedPackages.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (selectedPackages.isNotEmpty()) {
                // Multi-select context top bar
                TopAppBar(
                    title = { Text("${selectedPackages.size} selected") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        IconButton(onClick = {
                            viewModel.freezeSelected { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        }) {
                            Icon(Icons.Default.Build, contentDescription = "Freeze selection")
                        }
                        IconButton(onClick = {
                            viewModel.unfreezeSelected { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Unfreeze selection")
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel action")
                        }
                    }
                )
            } else {
                LargeTopAppBar(
                    title = { Text("FrozenDroid", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh lists")
                        }
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.List, contentDescription = "Filters catalog")
                        }
                    }
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "home",
                onNavigateToHome = {},
                onNavigateToProfiles = onNavigateToProfiles,
                onNavigateToCpu = onNavigateToCpu,
                onNavigateToSafeDirectory = onNavigateToSafeDirectory
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pinned SearchBar 
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_bar")
            )

            // Dynamic view selection depending on UiState
            Box(modifier = Modifier.weight(1f)) {
                when (uiState) {
                    is HomeUiState.Loading -> {
                        ShimmerSkeletonLoader()
                    }
                    is HomeUiState.Success -> {
                        val appsList = (uiState as HomeUiState.Success).apps
                        if (appsList.isEmpty()) {
                            EmptyStatePlaceholder("No matching app packages detected.")
                        } else {
                            AppLazyColumn(
                                apps = appsList,
                                selectedPackages = selectedPackages,
                                onToggleSelect = { viewModel.toggleSelection(it) },
                                onFreeze = { pkg ->
                                    viewModel.freezeApp(pkg) { msg ->
                                        scope.launch {
                                            val act = snackbarHostState.showSnackbar(
                                                message = msg,
                                                actionLabel = "UNDO",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (act == SnackbarResult.ActionPerformed) {
                                                viewModel.undoLastFreeze { undoMsg ->
                                                    scope.launch { snackbarHostState.showSnackbar(undoMsg) }
                                                }
                                            }
                                        }
                                    }
                                },
                                onUnfreeze = { pkg ->
                                    viewModel.unfreezeApp(pkg) { msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                }
                            )
                        }
                    }
                    is HomeUiState.Error -> {
                        val errMsg = (uiState as HomeUiState.Error).message
                        ErrorPlaceholder(errMsg)
                    }
                }
            }
        }
    }

    // Modal Filter bottom sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            FilterBottomSheetContent(
                state = filterState,
                onStateChanged = { viewModel.setFilters(it) },
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search installed packages...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = modifier
    )
}

@Composable
fun AppLazyColumn(
    apps: List<AppPackage>,
    selectedPackages: Set<String>,
    onToggleSelect: (String) -> Unit,
    onFreeze: (String) -> Unit,
    onUnfreeze: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            val isSelected = selectedPackages.contains(app.packageName)
            AppCardItem(
                app = app,
                isSelected = isSelected,
                onLongClick = { onToggleSelect(app.packageName) },
                onClick = {
                    if (selectedPackages.isNotEmpty()) {
                        onToggleSelect(app.packageName)
                    }
                },
                onFreeze = { onFreeze(app.packageName) },
                onUnfreeze = { onUnfreeze(app.packageName) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCardItem(
    app: AppPackage,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onFreeze: () -> Unit,
    onUnfreeze: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(
                onLongClick = {
                    scope.launch {
                        scale.animateTo(0.95f, animationSpec = spring())
                        scale.animateTo(1f, animationSpec = spring())
                    }
                    onLongClick()
                },
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceCardVariant()
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Avatar 
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (app.isFrozen) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (app.isFrozen) Icons.Default.Lock else Icons.Default.Star,
                    contentDescription = null,
                    tint = if (app.isFrozen) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    SafetyBadge(app.safetyLevel)
                    if (app.carrier != "Universal") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = app.carrier,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick Toggle Freeze Chip
            if (selectedPackages.isEmpty()) {
                if (app.isFrozen) {
                    FilledTonalButton(
                        onClick = onUnfreeze,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("unfreeze_button")
                    ) {
                        Text("Active", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onFreeze,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        enabled = app.safetyLevel != SafetyLevel.DANGEROUS,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("freeze_button")
                    ) {
                        Text("Freeze", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SafetyBadge(safetyLevel: SafetyLevel) {
    val color = when (safetyLevel) {
        SafetyLevel.SAFE -> Color(0xFF2E7D32)
        SafetyLevel.CAUTION -> Color(0xFFEF6C00)
        SafetyLevel.DANGEROUS -> Color(0xFFC62828)
    }
    Text(
        text = safetyLevel.name,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun ShimmerSkeletonLoader() {
    Column(modifier = Modifier.padding(16.dp)) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.15f))
            )
        }
    }
}

@Composable
fun FilterBottomSheetContent(
    state: FilterState,
    onStateChanged: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Classification Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        FilterCheckboxRow("Show SAFE packages", state.showSafe) {
            onStateChanged(state.copy(showSafe = it))
        }
        FilterCheckboxRow("Show CAUTION packages", state.showCaution) {
            onStateChanged(state.copy(showCaution = it))
        }
        FilterCheckboxRow("Show DANGEROUS core system", state.showDangerous) {
            onStateChanged(state.copy(showDangerous = it))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        FilterCheckboxRow("Show FROZEN only", state.showFrozenOnly) {
            onStateChanged(state.copy(showFrozenOnly = it, showActiveOnly = false))
        }
        FilterCheckboxRow("Show ACTIVE only", state.showActiveOnly) {
            onStateChanged(state.copy(showActiveOnly = it, showFrozenOnly = false))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Apply Options")
        }
    }
}

@Composable
fun FilterCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun EmptyStatePlaceholder(msg: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))
            Text(msg, color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
        }
    }
}

@Composable
fun ErrorPlaceholder(msg: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Text(msg, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToCpu: () -> Unit,
    onNavigateToSafeDirectory: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onNavigateToHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Manager") }
        )
        NavigationBarItem(
            selected = currentRoute == "profiles",
            onClick = onNavigateToProfiles,
            icon = { Icon(Icons.Outlined.Check, contentDescription = null) },
            label = { Text("Presets") }
        )
        NavigationBarItem(
            selected = currentRoute == "cpu",
            onClick = onNavigateToCpu,
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            label = { Text("Wakers") }
        )
        NavigationBarItem(
            selected = currentRoute == "directory",
            onClick = onNavigateToSafeDirectory,
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text("Catalog") }
        )
    }
}

fun MaterialTheme.colorScheme.surfaceCardVariant(): Color {
    return this.surfaceVariant.copy(alpha = 0.4f)
}
