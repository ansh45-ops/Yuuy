package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebhookConfigEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ControlPanelViewModel

@Composable
fun WebhooksScreen(
    viewModel: ControlPanelViewModel
) {
    val webhooks by viewModel.webhooks.collectAsState()
    val webhookLogs by viewModel.webhookLogs.collectAsState()
    val adminSettings by viewModel.adminSettings.collectAsState()

    var showAddWebhookDialog by remember { mutableStateOf(false) }
    var showEditOriginsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddWebhookDialog = true },
                containerColor = CyberCyan,
                contentColor = DarkBackground,
                modifier = Modifier.testTag("add_webhook_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Webhook")
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
            // Website Admin URL & Gateway Connector Card
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "WEBSITE ADMIN URL & API BRIDGE",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            StatusBadge(if (adminSettings?.isBridgeActive == true) "ACTIVE" else "PAUSED")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Connect this remote Admin URL to any website, Next.js dashboard, or custom backend to remotely control bots and receive notifications.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Admin URL Field
                        Text("Remote Admin Gateway URL:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = adminSettings?.adminBridgeUrl ?: "https://api.nexuscontrol.io/v1/admin/bridge",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Admin URL", adminSettings?.adminBridgeUrl ?: ""))
                                        viewModel.showFeedback("Admin Gateway URL copied!")
                                    },
                                    modifier = Modifier.size(28.dp).testTag("copy_admin_url_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Admin URL", tint = CyberCyan, modifier = Modifier.size(15.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secret Bearer API Key
                        Text("Secret Admin API Key:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = adminSettings?.secretApiKey ?: "nxc_live_auth_••••••••",
                                    color = CodeGreen,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                Row {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Secret API Key", adminSettings?.secretApiKey ?: ""))
                                            viewModel.showFeedback("API Key copied!")
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Key", tint = CodeGreen, modifier = Modifier.size(15.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.regenerateAdminSecret() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate Key", tint = TextMuted, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Allowed CORS Origins
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Allowed Website Origins:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = adminSettings?.allowedOrigins ?: "*",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { showEditOriginsDialog = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Origins", tint = CyberCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Embed Snippet Code Card
            item {
                CopyableCodeCard(
                    title = "WEBSITE EMBED SNIPPET (JS / FETCH)",
                    code = """// Embed into your website admin panel
const response = await fetch("${adminSettings?.adminBridgeUrl ?: "https://api.nexuscontrol.io/v1/admin/bridge"}", {
  method: "POST",
  headers: {
    "Authorization": "Bearer ${adminSettings?.secretApiKey ?: "nxc_live_auth_key"}",
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    action: "GET_BOT_STATUS",
    botId: "all"
  })
});
const data = await response.json();
console.log("NexusControl Remote Data:", data);"""
                )
            }

            // Webhook Endpoints Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CUSTOM NOTIFICATION WEBHOOKS",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Button(
                        onClick = { showAddWebhookDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Webhook", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Webhook Cards
            items(webhooks, key = { it.id }) { webhook ->
                WebhookCardItem(
                    webhook = webhook,
                    onTest = { viewModel.testWebhook(webhook) },
                    onToggleActive = { viewModel.toggleWebhookActive(webhook) },
                    onDelete = { viewModel.deleteWebhook(webhook) }
                )
            }

            // Webhook Event Logs History
            item {
                Text(
                    text = "REAL-TIME EVENT DELIVERY LOGS",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(webhookLogs, key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StatusBadge(status = if (log.responseCode in 200..299) "SUCCESS" else "FAILED")
                                Text(
                                    text = log.eventType,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = log.payloadJson,
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "HTTP ${log.responseCode}",
                                color = if (log.responseCode in 200..299) NeonGreen else NeonCoral,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${log.latencyMs}ms",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddWebhookDialog) {
        AddWebhookDialog(
            onDismiss = { showAddWebhookDialog = false },
            onConfirm = { name, targetUrl, direction, eventFilters ->
                viewModel.addNewWebhook(name, targetUrl, direction, eventFilters)
                showAddWebhookDialog = false
            }
        )
    }

    if (showEditOriginsDialog && adminSettings != null) {
        EditOriginsDialog(
            currentOrigins = adminSettings?.allowedOrigins ?: "",
            onDismiss = { showEditOriginsDialog = false },
            onConfirm = { newOrigins ->
                viewModel.updateAllowedOrigins(newOrigins)
                showEditOriginsDialog = false
            }
        )
    }
}

@Composable
fun WebhookCardItem(
    webhook: WebhookConfigEntity,
    onTest: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("webhook_card_${webhook.id}"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (webhook.direction == "OUTGOING") CyberIndigo.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = webhook.direction,
                            color = if (webhook.direction == "OUTGOING") CyberIndigo else CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = webhook.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                StatusBadge(if (webhook.isActive) "ACTIVE" else "PAUSED")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = webhook.targetUrl,
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Events: ${webhook.eventFilters}",
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = DarkBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTest,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp).testTag("test_webhook_${webhook.id}")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dispatch Test", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${webhook.successCount} sent",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Switch(
                        checked = webhook.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkBackground,
                            checkedTrackColor = CyberCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}
