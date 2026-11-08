package com.studio.rebuild

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class NavItem(val label: String, val icon: ImageVector)

class MainActivity : ComponentActivity() {
    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RebuildStudioTheme {
                val state by viewModel.uiState.collectAsState()

                val navItems = listOf(
                    NavItem("Home", Icons.Default.Home),
                    NavItem("Research", Icons.Default.Search),
                    NavItem("Blueprint", Icons.Default.Architecture),
                    NavItem("Build", Icons.Default.Code),
                    NavItem("Improve", Icons.Default.TrendingUp),
                    NavItem("Export", Icons.Default.FileDownload)
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = NavigationBarDefaults.Elevation
                        ) {
                            navItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = state.currentTabIndex == index,
                                    onClick = { viewModel.setTabIndex(index) },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (state.currentTabIndex) {
                            0 -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateNext = { viewModel.setTabIndex(1) }
                            )
                            1 -> ResearchScreen(viewModel = viewModel)
                            2 -> BlueprintScreen(viewModel = viewModel)
                            3 -> BuildScreen(viewModel = viewModel)
                            4 -> ImproveScreen(viewModel = viewModel)
                            5 -> ExportScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// LIVE RUNTIME PREVIEW
// ==========================================
data class PreviewHabitItem(
    val id: Long,
    val title: String,
    val streakCount: Int,
    val isCompletedToday: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePreviewContainer(
    appName: String,
    brandColor: Color,
    modifier: Modifier = Modifier
) {
    var habits by remember {
        mutableStateOf(
            listOf(
                PreviewHabitItem(1, "Deep Focus (45 mins)", 12, false),
                PreviewHabitItem(2, "Hydrate (2.5L)", 5, true),
                PreviewHabitItem(3, "Evening Stretches", 2, false)
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newHabitTitle by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(520.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(appName, style = MaterialTheme.typography.titleMedium)
                            Text("Interactive Runtime Simulator", style = MaterialTheme.typography.labelSmall, color = brandColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = brandColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(habits, key = { it.id }) { item ->
                        HabitInteractiveCard(
                            habit = item,
                            accentColor = brandColor,
                            onToggle = {
                                habits = habits.map {
                                    if (it.id == item.id) {
                                        val newState = !it.isCompletedToday
                                        it.copy(
                                            isCompletedToday = newState,
                                            streakCount = if (newState) it.streakCount + 1 else it.streakCount - 1
                                        )
                                    } else it
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Quick Register Habit") },
            text = {
                OutlinedTextField(
                    value = newHabitTitle,
                    onValueChange = { newHabitTitle = it },
                    label = { Text("Habit Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newHabitTitle.isNotBlank()) {
                            habits = habits + PreviewHabitItem(System.currentTimeMillis(), newHabitTitle, 1, false)
                            newHabitTitle = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HabitInteractiveCard(
    habit: PreviewHabitItem,
    accentColor: Color,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(habit.title, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${habit.streakCount} day streak", style = MaterialTheme.typography.labelSmall)
                }
            }

            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (habit.isCompletedToday) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = if (habit.isCompletedToday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==========================================
// SCREEN 1: HOME
// ==========================================
@Composable
fun HomeScreen(
    viewModel: StudioViewModel,
    onNavigateNext: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val input = state.currentInput
    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { viewModel.addReferenceImage(it.toString()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Rebuild Studio",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Deconstruct popular product patterns, analyze workflows safely, and generate original native Android applications.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        OutlinedTextField(
            value = input.referenceAppName,
            onValueChange = { viewModel.updateInput(input.copy(referenceAppName = it)) },
            label = { Text("Reference Product / Category") },
            placeholder = { Text("e.g. Habitica, Strava") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = input.originalBrandName,
            onValueChange = { viewModel.updateInput(input.copy(originalBrandName = it)) },
            label = { Text("Your Brand Name") },
            placeholder = { Text("e.g. Rhythm Engine") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = input.uniqueDifferentiator,
            onValueChange = { viewModel.updateInput(input.copy(uniqueDifferentiator = it)) },
            label = { Text("Differentiators & Privacy Improvements") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Reference Screenshots (Workflow Analysis Only)", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Used only to determine UX structure. No proprietary assets are reused.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Reference Images")
                }

                if (input.referenceImageUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(input.referenceImageUris) { uriString ->
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = uriString,
                                    contentDescription = "Reference",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { viewModel.removeReferenceImage(uriString) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.isGenerating) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        state.generationProgressMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        state.errorMessage?.let { err ->
            Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.runSynthesis()
                    onNavigateNext()
                },
                enabled = !state.isGenerating,
                modifier = Modifier.weight(1f).height(54.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Alternative")
            }

            OutlinedButton(
                onClick = { viewModel.loadStarterProject() },
                modifier = Modifier.height(54.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// SCREEN 2: RESEARCH
// ==========================================
@Composable
fun ResearchScreen(viewModel: StudioViewModel) {
    val state by viewModel.uiState.collectAsState()
    val report = state.researchReport

    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No research data generated yet. Configure your project on Home.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Observed Market Functionality", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(report.observedFunctionality, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Text("User Jobs-to-be-Done", style = MaterialTheme.typography.titleMedium)
        }
        items(report.targetAudienceAndJtbd) { jtbd ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(text = jtbd, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            Text("Opportunities for Market Differentiation", style = MaterialTheme.typography.titleMedium)
        }
        items(report.marketOpportunityGaps) { opportunity ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(text = "★ $opportunity", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("IP Safety & Clean-Room Verification", style = MaterialTheme.typography.titleMedium)
            }
        }
        items(report.legalIpSafetyChecklist) { check ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(check.title, style = MaterialTheme.typography.labelLarge)
                        Text(check.advisoryNote, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: BLUEPRINT
// ==========================================
@Composable
fun BlueprintScreen(viewModel: StudioViewModel) {
    val state by viewModel.uiState.collectAsState()
    val blueprint = state.blueprint ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Information Architecture & Screens", style = MaterialTheme.typography.titleMedium)
        }
        items(blueprint.informationArchitecture) { route ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(route, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            Text("User Flows: Happy Paths", style = MaterialTheme.typography.titleMedium)
        }
        items(blueprint.happyPathUserFlows) { flow ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(flow, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            Text("Scope Demarcation (MVP vs Phase 2)", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("MVP Scope", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        blueprint.mvpScopeBoundaries.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Phase 2 Scope", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        blueprint.phaseTwoScopeBoundaries.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: BUILD (PREVIEW + EDITOR)
// ==========================================
@Composable
fun BuildScreen(viewModel: StudioViewModel) {
    val state by viewModel.uiState.collectAsState()
    val scaffold = state.scaffold ?: return
    var selectedTab by remember { mutableStateOf(0) }
    var codeBuffer by remember(scaffold.editableUiCode) { mutableStateOf(scaffold.editableUiCode) }

    val tabTitles = listOf("Live Preview Sandbox", "Jetpack Compose Editor", "Project Structure")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Interactive Runtime Sandbox", style = MaterialTheme.typography.titleMedium)
                    val brandColor = try {
                        Color(android.graphics.Color.parseColor(state.currentInput.primaryColorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    LivePreviewContainer(
                        appName = state.currentInput.originalBrandName,
                        brandColor = brandColor
                    )
                }
            }
            1 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("DashboardScreen.kt", style = MaterialTheme.typography.labelLarge)
                        Button(
                            onClick = {
                                viewModel.updateScaffoldUiCode(codeBuffer)
                                viewModel.saveCurrentProject()
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = codeBuffer,
                        onValueChange = { codeBuffer = it },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }
            2 -> {
                Card(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = scaffold.directoryStructure,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 5: IMPROVE
// ==========================================
@Composable
fun ImproveScreen(viewModel: StudioViewModel) {
    val state by viewModel.uiState.collectAsState()
    val plan = state.improvementPlan ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("10 Differentiated Features", style = MaterialTheme.typography.titleMedium)
        }
        items(plan.tenUniqueFeatures) { feature ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(feature, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            Text("Friction-Free Onboarding Strategy", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(plan.frictionFreeOnboarding, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            Text("Independent Monetization Model", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(plan.monetizationStrategy, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// ==========================================
// SCREEN 6: EXPORT
// ==========================================
@Composable
fun ExportScreen(viewModel: StudioViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val markdownExport = remember(state) {
        buildString {
            appendLine("# Rebuild Studio Specification: ${state.currentInput.originalBrandName}")
            appendLine("Generated clean-room architecture specification and improvement blueprint.\n")
            appendLine("## 1. Product Research Assumptions")
            appendLine(state.researchReport?.observedFunctionality ?: "N/A")
            appendLine("\n### Target Audience & Core JTBD")
            state.researchReport?.targetAudienceAndJtbd?.forEach { appendLine("- $it") }
            appendLine("\n## 2. Differentiating Improvements")
            state.improvementPlan?.tenUniqueFeatures?.forEach { appendLine(it) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Export Specification", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Project Markdown Document", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Markdown Specs", markdownExport))
                        Toast.makeText(context, "Markdown report copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Full Report to Clipboard")
                }
            }
        }
    }
}
