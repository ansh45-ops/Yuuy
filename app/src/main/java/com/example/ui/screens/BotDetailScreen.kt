package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.BotUserEntity
import com.example.ui.components.AddUserDialog
import com.example.ui.components.EditScriptDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.ControlPanelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotDetailScreen(
    botId: String,
    viewModel: ControlPanelViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    LaunchedEffect(botId) {
        viewModel.selectBot(botId)
    }

    val bots by viewModel.bots.collectAsState()
    val bot = bots.firstOrNull { it.id == botId }

    val users by viewModel.botUsers.collectAsState()
    val tokenVerification by viewModel.tokenVerificationState.collectAsState()
    val isVerifyingToken by viewModel.isVerifyingToken.collectAsState()

    var userSearchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showEditScriptDialog by remember { mutableStateOf(false) }

    val filteredUsers = users.filter { user ->
        val matchesSearch = user.username.contains(userSearchQuery, ignoreCase = true) ||
                user.displayName.contains(userSearchQuery, ignoreCase = true) ||
                user.telegramUserId.toString().contains(userSearchQuery)
        val matchesRole = when (selectedRoleFilter) {
            "ALL" -> true
            else -> user.role == selectedRoleFilter
        }
        matchesSearch && matchesRole
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = bot?.name ?: "Bot Controller",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = bot?.username ?: "",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("bot_detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    bot?.let { currentBot ->
                        IconButton(onClick = { viewModel.restartBot(currentBot) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = CyberCyan)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                containerColor = CyberCyan,
                contentColor = DarkBackground,
                modifier = Modifier.testTag("add_bot_user_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add User")
            }
        }
    ) { paddingValues ->
        if (bot == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyberCyan)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
        ) {
            // Bot System Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatusBadge(status = bot.status)
                                Text(
                                    text = "Host: ${bot.hostServerName}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Button(
                                onClick = { viewModel.toggleBotStatus(bot) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (bot.status == "RUNNING") NeonCoral.copy(alpha = 0.2f) else NeonGreen.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = if (bot.status == "RUNNING") "Stop Bot" else "Start Bot",
                                    color = if (bot.status == "RUNNING") NeonCoral else NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Running Script
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Running Script / Daemon:", color = TextSecondary, fontSize = 12.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TerminalBg)
                                    .clickable { showEditScriptDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Code, contentDescription = null, tint = CodeGreen, modifier = Modifier.size(12.dp))
                                    Text(bot.scriptFile, color = CodeGreen, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextMuted, modifier = Modifier.size(10.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Token Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bot Token:", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = bot.token.take(14) + "••••••••••••",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Live Telegram API Diagnostic Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "TELEGRAM API LIVE CHECK",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Button(
                                onClick = { viewModel.verifyTelegramToken(bot.token) },
                                enabled = !isVerifyingToken,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp).testTag("ping_telegram_api_button")
                            ) {
                                if (isVerifyingToken) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = TextPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ping API", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        tokenVerification?.let { result ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (result.isSuccess) NeonGreen.copy(alpha = 0.12f) else NeonCoral.copy(alpha = 0.12f))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (result.isSuccess) NeonGreen else NeonCoral,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (result.isSuccess) "Telegram Gateway Connected (${result.latencyMs}ms)" else "Gateway Error",
                                            color = if (result.isSuccess) NeonGreen else NeonCoral,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    if (result.isSuccess) {
                                        Text("Telegram Bot ID: ${result.botId}", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        Text("Webhook: ${result.webhookUrl}", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    } else {
                                        Text(result.errorMessage ?: "", color = NeonCoral, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // User Permissions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BOT USERS & ACCESS CONTROL",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Grant roles & granular permissions per Telegram user",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { showAddUserDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add User", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // User Search & Role Filters
            item {
                OutlinedTextField(
                    value = userSearchQuery,
                    onValueChange = { userSearchQuery = it },
                    placeholder = { Text("Search users by username, name or ID...", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.fillMaxWidth().testTag("search_bot_users_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL", "OWNER", "ADMIN", "MODERATOR", "VIP", "BANNED").forEach { role ->
                        FilterChip(
                            selected = selectedRoleFilter == role,
                            onClick = { selectedRoleFilter = role },
                            label = { Text(role, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }
            }

            // User Cards with Granular Permission Switches
            if (filteredUsers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PeopleOutline, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Users Match Criteria", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Use the + Add User button to grant bot permissions.", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(filteredUsers, key = { it.id }) { user ->
                    BotUserPermissionCard(
                        user = user,
                        onTogglePermission = { permKey -> viewModel.toggleUserPermission(user, permKey) },
                        onUpdateRole = { newRole -> viewModel.updateUserRole(user, newRole) },
                        onDeleteUser = { viewModel.deleteBotUser(user) }
                    )
                }
            }
        }
    }

    if (showAddUserDialog) {
        AddUserDialog(
            botId = botId,
            onDismiss = { showAddUserDialog = false },
            onConfirm = { telegramId, username, displayName, role ->
                viewModel.addBotUser(botId, telegramId, username, displayName, role)
                showAddUserDialog = false
            }
        )
    }

    if (showEditScriptDialog && bot != null) {
        EditScriptDialog(
            currentScript = bot.scriptFile,
            onDismiss = { showEditScriptDialog = false },
            onConfirm = { newScript ->
                viewModel.updateBotScript(bot, newScript)
                showEditScriptDialog = false
            }
        )
    }
}

@Composable
fun BotUserPermissionCard(
    user: BotUserEntity,
    onTogglePermission: (String) -> Unit,
    onUpdateRole: (String) -> Unit,
    onDeleteUser: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_card_${user.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // User Header
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when (user.role) {
                                    "OWNER" -> CyberCyan.copy(alpha = 0.2f)
                                    "ADMIN" -> CyberIndigo.copy(alpha = 0.2f)
                                    "BANNED" -> NeonCoral.copy(alpha = 0.2f)
                                    else -> DarkSurfaceElevated
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName.take(1).uppercase(),
                            color = when (user.role) {
                                "OWNER" -> CyberCyan
                                "ADMIN" -> CyberIndigo
                                "BANNED" -> NeonCoral
                                else -> TextPrimary
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Column {
                        Text(user.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(user.username, color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Text("ID: ${user.telegramUserId}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                StatusBadge(status = user.role)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Role Switch Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("ADMIN", "MODERATOR", "VIP", "USER", "BANNED").forEach { role ->
                    AssistChip(
                        onClick = { onUpdateRole(role) },
                        label = { Text(role, fontSize = 9.sp, fontWeight = FontWeight.SemiBold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (user.role == role) CyberCyan.copy(alpha = 0.2f) else DarkSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (user.role == role) CyberCyan else DarkBorder
                        ),
                        modifier = Modifier.height(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = DarkBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Granular Permissions Grid
            Text(
                text = "GRANULAR ACCESS PERMISSIONS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PermissionToggleRow(
                    label = "Execute Commands",
                    description = "Can invoke bot slash commands & queries",
                    isChecked = user.canUseCommands,
                    onCheckedChange = { onTogglePermission("canUseCommands") }
                )
                PermissionToggleRow(
                    label = "Broadcast Messages",
                    description = "Can dispatch announcements to all bot subscribers",
                    isChecked = user.canBroadcast,
                    onCheckedChange = { onTogglePermission("canBroadcast") }
                )
                PermissionToggleRow(
                    label = "Full Admin Access",
                    description = "Can view telemetry, revoke tokens & modify settings",
                    isChecked = user.hasAdminAccess,
                    onCheckedChange = { onTogglePermission("hasAdminAccess") }
                )
                PermissionToggleRow(
                    label = "Premium / VIP Tier",
                    description = "Unlocks bypass rate-limits & premium plugins",
                    isChecked = user.isPremium,
                    onCheckedChange = { onTogglePermission("isPremium") }
                )
                PermissionToggleRow(
                    label = "Script Execution (SSH/Bash)",
                    description = "Can execute scripts on host server instance",
                    isChecked = user.canExecuteScripts,
                    onCheckedChange = { onTogglePermission("canExecuteScripts") }
                )
                PermissionToggleRow(
                    label = "Manage Other Users",
                    description = "Can grant permissions or ban other users",
                    isChecked = user.canManageUsers,
                    onCheckedChange = { onTogglePermission("canManageUsers") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDeleteUser,
                    colors = ButtonDefaults.textButtonColors(contentColor = NeonCoral)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remove User Access", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun PermissionToggleRow(
    label: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = TextMuted, fontSize = 10.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkBackground,
                checkedTrackColor = CyberCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurfaceElevated
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}
