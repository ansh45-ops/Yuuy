package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ControlPanelViewModel

enum class NavigationScreen(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
    BOTS("Bots", Icons.Default.SmartToy, "nav_bots"),
    SERVERS("Servers", Icons.Default.Dns, "nav_servers"),
    TASKS("Tasks", Icons.Default.Speed, "nav_tasks"),
    WEBHOOKS("Webhooks", Icons.Default.Webhook, "nav_webhooks")
}

class MainActivity : ComponentActivity() {

    private val viewModel: ControlPanelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusControlTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: ControlPanelViewModel) {
    var currentScreen by remember { mutableStateOf(NavigationScreen.DASHBOARD) }
    var activeBotDetailId by remember { mutableStateOf<String?>(null) }

    val userMessage by viewModel.userFeedbackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = DarkSurfaceElevated,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        topBar = {
            if (activeBotDetailId == null) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberCyan.copy(alpha = 0.15f))
                                    .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "NexusControl",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(NeonGreen)
                                    )
                                }
                                Text(
                                    text = "Bot & Server Command Plane",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Global Emergency Status / Quick Action
                        FilledTonalButton(
                            onClick = {
                                viewModel.showFeedback("Nexus Gateway connected • Ping 34ms")
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = DarkSurfaceElevated,
                                contentColor = CyberCyan
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = "LIVE: 34ms",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface
                    )
                )
            }
        },
        bottomBar = {
            if (activeBotDetailId == null) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp
                ) {
                    NavigationScreen.values().forEach { screen ->
                        val selected = currentScreen == screen
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label,
                                    tint = if (selected) CyberCyan else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) CyberCyan else TextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = CyberCyan.copy(alpha = 0.15f),
                                selectedIconColor = CyberCyan,
                                unselectedIconColor = TextSecondary,
                                selectedTextColor = CyberCyan,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag(screen.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val detailId = activeBotDetailId
            if (detailId != null) {
                BotDetailScreen(
                    botId = detailId,
                    viewModel = viewModel,
                    onNavigateBack = { activeBotDetailId = null }
                )
            } else {
                when (currentScreen) {
                    NavigationScreen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToBots = { currentScreen = NavigationScreen.BOTS },
                        onNavigateToBotDetail = { botId -> activeBotDetailId = botId },
                        onNavigateToServers = { currentScreen = NavigationScreen.SERVERS },
                        onNavigateToTasks = { currentScreen = NavigationScreen.TASKS },
                        onNavigateToWebhooks = { currentScreen = NavigationScreen.WEBHOOKS }
                    )
                    NavigationScreen.BOTS -> BotsScreen(
                        viewModel = viewModel,
                        onNavigateToBotDetail = { botId -> activeBotDetailId = botId }
                    )
                    NavigationScreen.SERVERS -> ServersScreen(
                        viewModel = viewModel
                    )
                    NavigationScreen.TASKS -> ApiTasksScreen(
                        viewModel = viewModel
                    )
                    NavigationScreen.WEBHOOKS -> WebhooksScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
