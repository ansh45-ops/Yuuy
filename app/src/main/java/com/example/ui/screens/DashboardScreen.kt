package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotEntity
import com.example.data.model.ServerInstanceEntity
import com.example.ui.components.MetricGaugeCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.TerminalConsoleView
import com.example.ui.theme.*
import com.example.ui.viewmodel.ControlPanelViewModel

@Composable
fun DashboardScreen(
    viewModel: ControlPanelViewModel,
    onNavigateToBots: () -> Unit,
    onNavigateToBotDetail: (String) -> Unit,
    onNavigateToServers: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToWebhooks: () -> Unit
) {
    val bots by viewModel.bots.collectAsState()
    val servers by viewModel.servers.collectAsState()
    val tasks by viewModel.apiTasks.collectAsState()
    val webhooks by viewModel.webhooks.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()

    val runningBotsCount = bots.count { it.status == "RUNNING" }
    val totalUsers = bots.sumOf { it.activeUsersCount }
    val avgLatency = if (tasks.isNotEmpty()) tasks.map { it.lastLatencyMs }.average().toInt() else 0
    val avgSuccess = if (tasks.isNotEmpty()) tasks.map { it.successRatePercent }.average().toFloat() else 100f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // System Status Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("system_status_banner"),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonGreen.copy(alpha = 0.15f))
                                .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
                                )
                                Text(
                                    text = "CONTROL PLANE ONLINE",
                                    color = NeonGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "All bot daemons, API crons & server nodes active",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.showFeedback("Telemetry synchronized with cloud bridge") },
                        modifier = Modifier.testTag("refresh_telemetry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = CyberCyan
                        )
                    }
                }
            }
        }

        // Quick Emergency Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Command:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.emergencyPauseAllBots() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCoral.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("pause_all_bots_button")
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, tint = NeonCoral, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause All Bots", color = NeonCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.emergencyResumeAllBots() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("resume_all_bots_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume All", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Key Metrics 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricGaugeCard(
                        title = "TELEGRAM BOTS",
                        value = "$runningBotsCount / ${bots.size}",
                        subValue = "$totalUsers Managed Users",
                        progress = if (bots.isNotEmpty()) runningBotsCount.toFloat() / bots.size else 0f,
                        progressColor = CyberCyan,
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f).clickable { onNavigateToBots() }
                    )
                    MetricGaugeCard(
                        title = "CLOUD SERVERS",
                        value = "${servers.count { it.status == "RUNNING" }} Nodes",
                        subValue = "${servers.sumOf { it.activeContainersCount }} Containers Up",
                        progress = 0.85f,
                        progressColor = CyberIndigo,
                        icon = { Icon(Icons.Default.Dns, contentDescription = null, tint = CyberIndigo, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f).clickable { onNavigateToServers() }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricGaugeCard(
                        title = "API SUCCESS RATE",
                        value = "${"%.1f".format(avgSuccess)}%",
                        subValue = "Avg ${avgLatency}ms Latency",
                        progress = avgSuccess / 100f,
                        progressColor = NeonGreen,
                        icon = { Icon(Icons.Default.Speed, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f).clickable { onNavigateToTasks() }
                    )
                    MetricGaugeCard(
                        title = "ACTIVE WEBHOOKS",
                        value = "${webhooks.count { it.isActive }} Endpoints",
                        subValue = "${webhooks.sumOf { it.successCount }} Events Ingested",
                        progress = 0.95f,
                        progressColor = NeonAmber,
                        icon = { Icon(Icons.Default.Webhook, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f).clickable { onNavigateToWebhooks() }
                    )
                }
            }
        }

        // Active Telegram Bots Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE TELEGRAM BOTS",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                TextButton(onClick = onNavigateToBots) {
                    Text("View All (${bots.size})", color = CyberCyan, fontSize = 12.sp)
                }
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(bots) { bot ->
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clickable { onNavigateToBotDetail(bot.id) },
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(status = bot.status)
                                Text(
                                    text = "${bot.lastPingMs}ms",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = bot.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                            Text(
                                text = bot.username,
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                                Text(
                                    text = bot.scriptFile,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${bot.activeUsersCount} users", color = TextSecondary, fontSize = 11.sp)
                                Text("${bot.totalCommandsProcessed} cmds", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Server Nodes Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SERVER CLUSTERS",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                TextButton(onClick = onNavigateToServers) {
                    Text("Manage Servers", color = CyberCyan, fontSize = 12.sp)
                }
            }
        }

        items(servers) { server ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(server.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${server.ipAddress}:${server.sshPort} • ${server.region}", color = TextSecondary, fontSize = 11.sp)
                        }
                        StatusBadge(server.status)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // CPU Meter
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("CPU Load", color = TextMuted, fontSize = 10.sp)
                                Text("${"%.1f".format(server.cpuPercent)}%", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { server.cpuPercent / 100f },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = if (server.cpuPercent > 80f) NeonCoral else if (server.cpuPercent > 50f) NeonAmber else NeonGreen,
                                trackColor = DarkSurfaceElevated
                            )
                        }
                        // RAM Meter
                        Column(modifier = Modifier.weight(1f)) {
                            val ramPercent = server.ramUsedMb.toFloat() / server.ramTotalMb
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("RAM", color = TextMuted, fontSize = 10.sp)
                                Text("${server.ramUsedMb}/${server.ramTotalMb} MB", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ramPercent },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = CyberCyan,
                                trackColor = DarkSurfaceElevated
                            )
                        }
                    }
                }
            }
        }

        // Live Interactive Terminal Console View
        item {
            Text(
                text = "INTERACTIVE CLOUD CONSOLE",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            TerminalConsoleView(
                logs = terminalLogs,
                onExecuteCommand = { cmd -> viewModel.executeTerminalCommand(cmd) }
            )
        }
    }
}
