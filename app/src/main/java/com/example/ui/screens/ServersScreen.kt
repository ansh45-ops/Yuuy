package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServerInstanceEntity
import com.example.ui.components.AddServerDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.TerminalConsoleView
import com.example.ui.theme.*
import com.example.ui.viewmodel.ControlPanelViewModel

@Composable
fun ServersScreen(
    viewModel: ControlPanelViewModel
) {
    val servers by viewModel.servers.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    var showAddServerDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddServerDialog = true },
                containerColor = CyberCyan,
                contentColor = DarkBackground,
                modifier = Modifier.testTag("add_server_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Server")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
        ) {
            // Header Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("ACTIVE NODES", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${servers.count { it.status == "RUNNING" }} / ${servers.size}", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("CONTAINERS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${servers.sumOf { it.activeContainersCount }} Running", color = CyberCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Server Instances
            items(servers, key = { it.id }) { server ->
                ServerInstanceCardItem(
                    server = server,
                    onReboot = { viewModel.rebootServer(server) },
                    onTogglePower = { viewModel.toggleServerPower(server) },
                    onDelete = { viewModel.deleteServer(server) }
                )
            }

            // Terminal Console
            item {
                Text(
                    text = "SSH TELEMETRY & CLOUD DAEMON LOGS",
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

    if (showAddServerDialog) {
        AddServerDialog(
            onDismiss = { showAddServerDialog = false },
            onConfirm = { name, ip, port, region, os, ram, disk ->
                viewModel.addNewServer(name, ip, port, region, os, ram, disk)
                showAddServerDialog = false
            }
        )
    }
}

@Composable
fun ServerInstanceCardItem(
    server: ServerInstanceEntity,
    onReboot: () -> Unit,
    onTogglePower: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_card_${server.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = CyberIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(server.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = "${server.ipAddress}:${server.sshPort} • ${server.region}",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                StatusBadge(server.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "OS: ${server.osInfo} • ${server.activeContainersCount} Docker services",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Resource Gauges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // CPU Gauge
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CPU", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${"%.1f".format(server.cpuPercent)}%", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { server.cpuPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (server.cpuPercent > 80f) NeonCoral else if (server.cpuPercent > 50f) NeonAmber else NeonGreen,
                        trackColor = DarkSurfaceElevated
                    )
                }

                // RAM Gauge
                Column(modifier = Modifier.weight(1f)) {
                    val ramFrac = server.ramUsedMb.toFloat() / server.ramTotalMb
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("RAM", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${server.ramUsedMb / 1024}G / ${server.ramTotalMb / 1024}G", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { ramFrac },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = CyberCyan,
                        trackColor = DarkSurfaceElevated
                    )
                }

                // Disk Gauge
                Column(modifier = Modifier.weight(1f)) {
                    val diskFrac = server.diskUsedGb.toFloat() / server.diskTotalGb
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("DISK", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${server.diskUsedGb}G / ${server.diskTotalGb}G", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { diskFrac },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = CyberIndigo,
                        trackColor = DarkSurfaceElevated
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = DarkBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onReboot,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("reboot_server_${server.id}")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reboot", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onTogglePower,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (server.status == "RUNNING") NeonCoral.copy(alpha = 0.15f) else NeonGreen.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (server.status == "RUNNING") NeonCoral else NeonGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (server.status == "RUNNING") "Power Off" else "Power On",
                            color = if (server.status == "RUNNING") NeonCoral else NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
