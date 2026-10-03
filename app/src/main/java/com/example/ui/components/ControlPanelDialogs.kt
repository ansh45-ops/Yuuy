package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.network.TelegramBotCheckResult
import com.example.ui.theme.*

@Composable
fun AddBotDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, username: String, token: String, scriptFile: String, hostServer: String) -> Unit,
    onVerifyToken: (String) -> Unit,
    verificationResult: TelegramBotCheckResult?,
    isVerifying: Boolean
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var scriptFile by remember { mutableStateOf("bot_main.py") }
    var hostServer by remember { mutableStateOf("US-East-Prod-01") }

    LaunchedEffect(verificationResult) {
        verificationResult?.let { res ->
            if (res.isSuccess) {
                if (name.isBlank() && res.botName != null) name = res.botName
                if (username.isBlank() && res.username != null) username = res.username
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = CyberCyan
                    )
                    Text(
                        text = "Register Telegram Bot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Add your Telegram Bot token from @BotFather. You can test and verify it live against the Telegram Bot API.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Token Input & Test Button
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("Bot API Token") },
                    placeholder = { Text("123456789:ABCdef-...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bot_token_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onVerifyToken(token) },
                        enabled = token.isNotBlank() && !isVerifying,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberIndigo),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("verify_token_button")
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = TextPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...")
                        } else {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verify with Telegram API", fontSize = 12.sp)
                        }
                    }
                }

                // Verification status banner
                verificationResult?.let { res ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (res.isSuccess) NeonGreen.copy(alpha = 0.15f) else NeonCoral.copy(alpha = 0.15f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (res.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (res.isSuccess) NeonGreen else NeonCoral,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (res.isSuccess) "Token Verified! (${res.latencyMs}ms)" else "Verification Failed",
                                    color = if (res.isSuccess) NeonGreen else NeonCoral,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            if (res.isSuccess) {
                                Text(
                                    text = "Name: ${res.botName} | User: ${res.username ?: "N/A"}",
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = res.webhookUrl ?: "",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                Text(
                                    text = res.errorMessage ?: "Unknown error",
                                    fontSize = 11.sp,
                                    color = NeonCoral
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bot Display Name") },
                    placeholder = { Text("e.g. My Telegram Bot") },
                    modifier = Modifier.fillMaxWidth().testTag("bot_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Bot Username") },
                    placeholder = { Text("@my_bot") },
                    modifier = Modifier.fillMaxWidth().testTag("bot_username_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                OutlinedTextField(
                    value = scriptFile,
                    onValueChange = { scriptFile = it },
                    label = { Text("Active Running Script / File") },
                    placeholder = { Text("e.g. bot_main.py, worker.js") },
                    modifier = Modifier.fillMaxWidth().testTag("bot_script_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                OutlinedTextField(
                    value = hostServer,
                    onValueChange = { hostServer = it },
                    label = { Text("Host Server Instance") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(name, username, token, scriptFile, hostServer)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        enabled = token.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_bot_button")
                    ) {
                        Text("Deploy & Save", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddServerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, ip: String, port: Int, region: String, os: String, ramMb: Int, diskGb: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var region by remember { mutableStateOf("us-east-1") }
    var osInfo by remember { mutableStateOf("Ubuntu 24.04 LTS") }
    var ramMb by remember { mutableStateOf("8192") }
    var diskGb by remember { mutableStateOf("160") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = CyberCyan)
                    Text(
                        "Attach Server Instance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Server Name") },
                    placeholder = { Text("e.g. EU-Hetzner-Node-01") },
                    modifier = Modifier.fillMaxWidth().testTag("server_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ip,
                        onValueChange = { ip = it },
                        label = { Text("IP Address") },
                        placeholder = { Text("198.51.100.1") },
                        modifier = Modifier.weight(2f).testTag("server_ip_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                }

                OutlinedTextField(
                    value = region,
                    onValueChange = { region = it },
                    label = { Text("Cloud / Region") },
                    placeholder = { Text("e.g. us-east-1, Frankfurt") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                OutlinedTextField(
                    value = osInfo,
                    onValueChange = { osInfo = it },
                    label = { Text("OS / Distro") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ramMb,
                        onValueChange = { ramMb = it },
                        label = { Text("RAM (MB)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    OutlinedTextField(
                        value = diskGb,
                        onValueChange = { diskGb = it },
                        label = { Text("Disk (GB)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val portInt = port.toIntOrNull() ?: 22
                            val ramInt = ramMb.toIntOrNull() ?: 8192
                            val diskInt = diskGb.toIntOrNull() ?: 160
                            onConfirm(name, ip, portInt, region, osInfo, ramInt, diskInt)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        enabled = name.isNotBlank() && ip.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_server_button")
                    ) {
                        Text("Connect Node", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, url: String, cron: String, method: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://") }
    var cron by remember { mutableStateOf("*/5 * * * *") }
    var method by remember { mutableStateOf("POST") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = CyberCyan)
                    Text("New Automated API Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Task / Cron Name") },
                    placeholder = { Text("e.g. Daily Telegram Sync") },
                    modifier = Modifier.fillMaxWidth().testTag("task_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Target Endpoint URL") },
                    modifier = Modifier.fillMaxWidth().testTag("task_url_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cron,
                        onValueChange = { cron = it },
                        label = { Text("Cron Schedule") },
                        placeholder = { Text("*/5 * * * *") },
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    OutlinedTextField(
                        value = method,
                        onValueChange = { method = it },
                        label = { Text("Method") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(name, url, cron, method) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        enabled = name.isNotBlank() && url.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text("Schedule Task", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddWebhookDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetUrl: String, direction: String, eventFilters: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetUrl by remember { mutableStateOf("https://") }
    var direction by remember { mutableStateOf("OUTGOING") }
    var eventFilters by remember { mutableStateOf("bot.crash, user.grant_admin, server.high_cpu") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Webhook, contentDescription = null, tint = CyberCyan)
                    Text("Add Custom Webhook", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Webhook Name") },
                    placeholder = { Text("e.g. Discord Ops Alert Hook") },
                    modifier = Modifier.fillMaxWidth().testTag("webhook_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                OutlinedTextField(
                    value = targetUrl,
                    onValueChange = { targetUrl = it },
                    label = { Text("Target URL") },
                    placeholder = { Text("https://discord.com/api/webhooks/...") },
                    modifier = Modifier.fillMaxWidth().testTag("webhook_url_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = direction == "OUTGOING",
                        onClick = { direction = "OUTGOING" },
                        label = { Text("Outgoing (Push)") }
                    )
                    FilterChip(
                        selected = direction == "INCOMING",
                        onClick = { direction = "INCOMING" },
                        label = { Text("Incoming (Ingress)") }
                    )
                }

                OutlinedTextField(
                    value = eventFilters,
                    onValueChange = { eventFilters = it },
                    label = { Text("Event Triggers (comma separated)") },
                    placeholder = { Text("bot.crash, order.paid, alert.server") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(name, targetUrl, direction, eventFilters) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        enabled = name.isNotBlank() && targetUrl.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_webhook_button")
                    ) {
                        Text("Create Webhook", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddUserDialog(
    botId: String,
    onDismiss: () -> Unit,
    onConfirm: (telegramId: Long, username: String, displayName: String, role: String) -> Unit
) {
    var telegramIdStr by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("USER") }

    val roles = listOf("OWNER", "ADMIN", "MODERATOR", "VIP", "USER", "BANNED")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = CyberCyan)
                    Text("Grant Bot User Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                OutlinedTextField(
                    value = telegramIdStr,
                    onValueChange = { telegramIdStr = it },
                    label = { Text("Telegram User ID") },
                    placeholder = { Text("e.g. 78192048") },
                    modifier = Modifier.fillMaxWidth().testTag("user_telegram_id_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("@username") },
                    modifier = Modifier.fillMaxWidth().testTag("user_username_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    placeholder = { Text("e.g. Alex (Dev)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Text("Assigned Role:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roles.take(3).forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roles.drop(3).forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role, fontSize = 11.sp) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val id = telegramIdStr.toLongOrNull() ?: 10000000L
                            onConfirm(id, username, displayName, selectedRole)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        enabled = username.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_user_button")
                    ) {
                        Text("Grant Permissions", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditScriptDialog(
    currentScript: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var script by remember { mutableStateOf(currentScript) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = CyberCyan)
                    Text("Change Running Script", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Text(
                    "Specify the script file or entrypoint executed by this Telegram bot daemon (e.g. main.py, src/index.js, daemon.go).",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = script,
                    onValueChange = { script = it },
                    label = { Text("Running Script File") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_script_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(script) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        enabled = script.isNotBlank(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save & Hot-Reload", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditOriginsDialog(
    currentOrigins: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var origins by remember { mutableStateOf(currentOrigins) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = CyberCyan)
                    Text("Allowed Website Origins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Text(
                    "Enter the domains of your websites that are allowed to call the Admin Bridge URL (comma-separated).",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = origins,
                    onValueChange = { origins = it },
                    label = { Text("Allowed Origins (CORS)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(origins) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Update Origins", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
