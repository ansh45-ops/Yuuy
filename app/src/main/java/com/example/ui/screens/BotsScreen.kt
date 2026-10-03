package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotEntity
import com.example.ui.components.AddBotDialog
import com.example.ui.components.EditScriptDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.ControlPanelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotsScreen(
    viewModel: ControlPanelViewModel,
    onNavigateToBotDetail: (String) -> Unit
) {
    val bots by viewModel.bots.collectAsState()
    val tokenVerification by viewModel.tokenVerificationState.collectAsState()
    val isVerifyingToken by viewModel.isVerifyingToken.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBotForScript by remember { mutableStateOf<BotEntity?>(null) }
    val context = LocalContext.current

    val filteredBots = bots.filter { bot ->
        val matchesSearch = bot.name.contains(searchQuery, ignoreCase = true) ||
                bot.username.contains(searchQuery, ignoreCase = true) ||
                bot.scriptFile.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "RUNNING" -> bot.status == "RUNNING"
            "IDLE" -> bot.status == "IDLE"
            "STOPPED" -> bot.status == "STOPPED"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.clearTokenVerification()
                    showAddDialog = true
                },
                containerColor = CyberCyan,
                contentColor = DarkBackground,
                modifier = Modifier.testTag("add_bot_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bot")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search bots by name, @username or script file...", fontSize = 13.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_bots_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard
                    )
                )
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "RUNNING", "IDLE", "STOPPED").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }
            }

            // Bots List
            if (filteredBots.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Telegram Bots Found", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tap the + button below to register a new bot with your token.", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(filteredBots, key = { it.id }) { bot ->
                    BotCardItem(
                        bot = bot,
                        onNavigateToDetail = { onNavigateToBotDetail(bot.id) },
                        onToggleStatus = { viewModel.toggleBotStatus(bot) },
                        onRestart = { viewModel.restartBot(bot) },
                        onEditScript = { editingBotForScript = bot },
                        onDelete = { viewModel.deleteBot(bot) },
                        onCopyToken = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Telegram Token", bot.token))
                            viewModel.showFeedback("Token copied for ${bot.username}")
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddBotDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, username, token, scriptFile, hostServer ->
                viewModel.addNewBot(name, username, token, scriptFile, hostServer)
                showAddDialog = false
            },
            onVerifyToken = { token -> viewModel.verifyTelegramToken(token) },
            verificationResult = tokenVerification,
            isVerifying = isVerifyingToken
        )
    }

    editingBotForScript?.let { bot ->
        EditScriptDialog(
            currentScript = bot.scriptFile,
            onDismiss = { editingBotForScript = null },
            onConfirm = { newScript ->
                viewModel.updateBotScript(bot, newScript)
                editingBotForScript = null
            }
        )
    }
}

@Composable
fun BotCardItem(
    bot: BotEntity,
    onNavigateToDetail: () -> Unit,
    onToggleStatus: () -> Unit,
    onRestart: () -> Unit,
    onEditScript: () -> Unit,
    onDelete: () -> Unit,
    onCopyToken: () -> Unit
) {
    var showToken by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bot_card_${bot.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Bot Avatar + Name + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyberIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = bot.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = bot.username,
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                StatusBadge(status = bot.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // File and Host Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Running Script Tag (clickable to edit)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalBg)
                        .clickable { onEditScript() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = CodeGreen, modifier = Modifier.size(12.dp))
                        Text(
                            text = bot.scriptFile,
                            color = CodeGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Icon(Icons.Default.Edit, contentDescription = "Edit Script", tint = TextMuted, modifier = Modifier.size(10.dp))
                    }
                }

                // Server Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                        Text(
                            text = bot.hostServerName,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Token Viewer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Text(
                            text = if (showToken) bot.token else bot.token.take(10) + "••••••••••••••••",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                    Row {
                        IconButton(onClick = { showToken = !showToken }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Token",
                                tint = TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        IconButton(onClick = onCopyToken, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Token", tint = CyberCyan, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row: Users, Commands, Uptime
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ACTIVE USERS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${bot.activeUsersCount}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("COMMANDS RUN", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${bot.totalCommandsProcessed}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("PING", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${bot.lastPingMs} ms", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = DarkBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Manage Users & Permissions Button
                Button(
                    onClick = onNavigateToDetail,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("manage_users_button_${bot.id}")
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Users & Permissions", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Restart
                    IconButton(
                        onClick = onRestart,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    }
                    // Start / Stop
                    IconButton(
                        onClick = onToggleStatus,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (bot.status == "RUNNING") NeonCoral.copy(alpha = 0.15f) else NeonGreen.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = if (bot.status == "RUNNING") Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Status",
                            tint = if (bot.status == "RUNNING") NeonCoral else NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
