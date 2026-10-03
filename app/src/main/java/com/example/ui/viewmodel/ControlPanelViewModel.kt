package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.network.TelegramBotCheckResult
import com.example.data.repository.ControlPanelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class ControlPanelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ControlPanelRepository

    val bots: StateFlow<List<BotEntity>>
    val servers: StateFlow<List<ServerInstanceEntity>>
    val apiTasks: StateFlow<List<ApiTaskEntity>>
    val webhooks: StateFlow<List<WebhookConfigEntity>>
    val webhookLogs: StateFlow<List<WebhookEventLogEntity>>
    val adminSettings: StateFlow<AdminSettingsEntity?>

    private val _selectedBotId = MutableStateFlow<String?>(null)
    val selectedBotId: StateFlow<String?> = _selectedBotId.asStateFlow()

    private val _botUsers = MutableStateFlow<List<BotUserEntity>>(emptyList())
    val botUsers: StateFlow<List<BotUserEntity>> = _botUsers.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<String>>(listOf(
        "[$] NexusControl Daemon v3.2.0 initialized",
        "[*] Attached to remote bridge websocket: wss://gateway.nexuscontrol.io/v1",
        "[+] 3 Telegram worker threads active",
        "[+] Heartbeat telemetry: All node clusters healthy"
    ))
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    private val _tokenVerificationState = MutableStateFlow<TelegramBotCheckResult?>(null)
    val tokenVerificationState: StateFlow<TelegramBotCheckResult?> = _tokenVerificationState.asStateFlow()

    private val _isVerifyingToken = MutableStateFlow(false)
    val isVerifyingToken: StateFlow<Boolean> = _isVerifyingToken.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ControlPanelRepository(database.controlPanelDao())

        bots = repository.allBots
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        servers = repository.allServers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        apiTasks = repository.allTasks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        webhooks = repository.allWebhooks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        webhookLogs = repository.recentWebhookLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        adminSettings = repository.adminSettings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        // Sync bot users when selected bot changes
        viewModelScope.launch {
            _selectedBotId.collectLatest { botId ->
                if (botId != null) {
                    repository.getBotUsers(botId).collect { users ->
                        _botUsers.value = users
                    }
                } else {
                    _botUsers.value = emptyList()
                }
            }
        }

        // Auto-select first bot when bots load if none selected
        viewModelScope.launch {
            bots.collect { botList ->
                if (_selectedBotId.value == null && botList.isNotEmpty()) {
                    _selectedBotId.value = botList.first().id
                }
            }
        }

        // Live telemetry heartbeat simulator
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(4000)
                // Random subtle fluctuation in server CPU and Bot telemetry
                val currentServers = servers.value
                if (currentServers.isNotEmpty()) {
                    val targetServer = currentServers.random()
                    if (targetServer.status == "RUNNING") {
                        val deltaCpu = (Random.nextFloat() * 4f - 2f)
                        val newCpu = (targetServer.cpuPercent + deltaCpu).coerceIn(10f, 95f)
                        repository.updateServer(targetServer.copy(
                            cpuPercent = (newCpu * 10).toInt() / 10f,
                            lastChecked = System.currentTimeMillis()
                        ))
                    }
                }

                // Increment command counters for running bots occasionally
                val currentBots = bots.value
                if (currentBots.isNotEmpty()) {
                    val activeBot = currentBots.firstOrNull { it.status == "RUNNING" }
                    if (activeBot != null) {
                        repository.updateBot(activeBot.copy(
                            totalCommandsProcessed = activeBot.totalCommandsProcessed + Random.nextLong(1, 4),
                            uptimeSeconds = activeBot.uptimeSeconds + 4
                        ))
                    }
                }
            }
        }
    }

    fun selectBot(botId: String) {
        _selectedBotId.value = botId
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    fun showFeedback(msg: String) {
        _userFeedbackMessage.value = msg
    }

    // BOT ACTIONS
    fun toggleBotStatus(bot: BotEntity) {
        viewModelScope.launch {
            val nextStatus = when (bot.status) {
                "RUNNING" -> "STOPPED"
                "STOPPED", "ERROR", "IDLE" -> "RUNNING"
                else -> "RUNNING"
            }
            repository.updateBot(bot.copy(status = nextStatus))
            appendTerminalLog("[BOT] ${bot.name} status updated -> $nextStatus")
            _userFeedbackMessage.value = "${bot.name} is now $nextStatus"
        }
    }

    fun restartBot(bot: BotEntity) {
        viewModelScope.launch {
            repository.updateBot(bot.copy(status = "IDLE"))
            appendTerminalLog("[BOT] Restarting ${bot.username} (Reloading ${bot.scriptFile})...")
            delay(1200)
            repository.updateBot(bot.copy(status = "RUNNING", uptimeSeconds = 0))
            appendTerminalLog("[BOT] ${bot.username} successfully re-spawned with PID ${Random.nextInt(1000, 9999)}")
            _userFeedbackMessage.value = "${bot.name} restarted successfully!"
        }
    }

    fun updateBotScript(bot: BotEntity, newScript: String) {
        viewModelScope.launch {
            repository.updateBot(bot.copy(scriptFile = newScript.trim()))
            appendTerminalLog("[CONFIG] Updated active script for ${bot.name} to: $newScript")
            _userFeedbackMessage.value = "Running script updated"
        }
    }

    fun deleteBot(bot: BotEntity) {
        viewModelScope.launch {
            repository.deleteBot(bot)
            if (_selectedBotId.value == bot.id) {
                _selectedBotId.value = bots.value.firstOrNull { it.id != bot.id }?.id
            }
            appendTerminalLog("[BOT] Terminated & purged bot instance: ${bot.name}")
            _userFeedbackMessage.value = "Bot deleted"
        }
    }

    fun addNewBot(
        name: String,
        username: String,
        token: String,
        scriptFile: String,
        hostServer: String
    ) {
        viewModelScope.launch {
            val newBot = BotEntity(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "Telegram Bot" },
                username = if (username.startsWith("@")) username else "@$username",
                token = token.trim(),
                scriptFile = scriptFile.ifBlank { "main.py" },
                status = "RUNNING",
                uptimeSeconds = 0L,
                activeUsersCount = 1,
                totalCommandsProcessed = 0L,
                lastPingMs = Random.nextInt(25, 75),
                hostServerName = hostServer.ifBlank { "US-East-Prod-01" }
            )
            repository.insertBot(newBot)
            _selectedBotId.value = newBot.id

            // Create admin user for this new bot
            val adminUser = BotUserEntity(
                id = UUID.randomUUID().toString(),
                botId = newBot.id,
                telegramUserId = Random.nextLong(10000000L, 99999999L),
                username = "@admin_master",
                displayName = "Primary Admin",
                role = "OWNER",
                canUseCommands = true,
                canBroadcast = true,
                hasAdminAccess = true,
                isPremium = true,
                canExecuteScripts = true,
                canManageUsers = true,
                joinedAt = System.currentTimeMillis(),
                lastActiveAt = System.currentTimeMillis()
            )
            repository.insertBotUser(adminUser)

            appendTerminalLog("[NEW BOT] Initialized instance ${newBot.name} [${newBot.scriptFile}] on ${newBot.hostServerName}")
            _userFeedbackMessage.value = "Bot ${newBot.username} created & running!"
        }
    }

    fun verifyTelegramToken(token: String) {
        viewModelScope.launch {
            _isVerifyingToken.value = true
            _tokenVerificationState.value = null
            val result = repository.verifyTelegramToken(token)
            _tokenVerificationState.value = result
            _isVerifyingToken.value = false
            if (result.isSuccess) {
                appendTerminalLog("[TELEGRAM] Verified @${result.username ?: result.botName} via official API (${result.latencyMs}ms)")
            } else {
                appendTerminalLog("[TELEGRAM ERROR] Token verification failed: ${result.errorMessage}")
            }
        }
    }

    fun clearTokenVerification() {
        _tokenVerificationState.value = null
    }

    // USER PERMISSIONS ACTIONS
    fun toggleUserPermission(user: BotUserEntity, permissionKey: String) {
        viewModelScope.launch {
            val updated = when (permissionKey) {
                "canUseCommands" -> user.copy(canUseCommands = !user.canUseCommands)
                "canBroadcast" -> user.copy(canBroadcast = !user.canBroadcast)
                "hasAdminAccess" -> user.copy(hasAdminAccess = !user.hasAdminAccess)
                "isPremium" -> user.copy(isPremium = !user.isPremium)
                "canExecuteScripts" -> user.copy(canExecuteScripts = !user.canExecuteScripts)
                "canManageUsers" -> user.copy(canManageUsers = !user.canManageUsers)
                else -> user
            }
            repository.updateBotUser(updated)
            appendTerminalLog("[PERM] User ${user.username} permission '$permissionKey' toggled")
            _userFeedbackMessage.value = "Updated permissions for ${user.username}"
        }
    }

    fun updateUserRole(user: BotUserEntity, newRole: String) {
        viewModelScope.launch {
            val updated = when (newRole) {
                "OWNER" -> user.copy(
                    role = newRole,
                    hasAdminAccess = true,
                    canUseCommands = true,
                    canBroadcast = true,
                    canManageUsers = true,
                    canExecuteScripts = true,
                    isPremium = true
                )
                "ADMIN" -> user.copy(
                    role = newRole,
                    hasAdminAccess = true,
                    canUseCommands = true,
                    canBroadcast = true,
                    canManageUsers = true,
                    canExecuteScripts = false,
                    isPremium = true
                )
                "MODERATOR" -> user.copy(
                    role = newRole,
                    hasAdminAccess = false,
                    canUseCommands = true,
                    canBroadcast = true,
                    canManageUsers = true,
                    canExecuteScripts = false
                )
                "VIP" -> user.copy(
                    role = newRole,
                    hasAdminAccess = false,
                    canUseCommands = true,
                    canBroadcast = false,
                    isPremium = true
                )
                "BANNED" -> user.copy(
                    role = newRole,
                    canUseCommands = false,
                    canBroadcast = false,
                    hasAdminAccess = false,
                    isPremium = false,
                    canExecuteScripts = false,
                    canManageUsers = false
                )
                else -> user.copy(role = newRole, hasAdminAccess = false, canUseCommands = true)
            }
            repository.updateBotUser(updated)
            appendTerminalLog("[USER ROLE] ${user.username} role changed to $newRole")
            _userFeedbackMessage.value = "${user.username} is now $newRole"
        }
    }

    fun addBotUser(
        botId: String,
        telegramId: Long,
        username: String,
        displayName: String,
        role: String
    ) {
        viewModelScope.launch {
            val newUser = BotUserEntity(
                id = UUID.randomUUID().toString(),
                botId = botId,
                telegramUserId = telegramId,
                username = if (username.startsWith("@")) username else "@$username",
                displayName = displayName.ifBlank { username },
                role = role,
                canUseCommands = role != "BANNED",
                canBroadcast = role in listOf("OWNER", "ADMIN", "MODERATOR"),
                hasAdminAccess = role in listOf("OWNER", "ADMIN"),
                isPremium = role in listOf("OWNER", "ADMIN", "VIP"),
                canExecuteScripts = role == "OWNER",
                canManageUsers = role in listOf("OWNER", "ADMIN", "MODERATOR"),
                joinedAt = System.currentTimeMillis(),
                lastActiveAt = System.currentTimeMillis()
            )
            repository.insertBotUser(newUser)
            _userFeedbackMessage.value = "User ${newUser.username} added!"
        }
    }

    fun deleteBotUser(user: BotUserEntity) {
        viewModelScope.launch {
            repository.deleteBotUser(user)
            _userFeedbackMessage.value = "Removed user ${user.username}"
        }
    }

    // SERVER ACTIONS
    fun rebootServer(server: ServerInstanceEntity) {
        viewModelScope.launch {
            repository.updateServer(server.copy(status = "REBOOTING", cpuPercent = 5.0f))
            appendTerminalLog("[SSH] Triggering reboot on ${server.name} (${server.ipAddress})...")
            delay(1500)
            repository.updateServer(server.copy(status = "RUNNING", cpuPercent = 22.4f))
            appendTerminalLog("[SSH] ${server.name} back online. Systemd units loaded.")
            _userFeedbackMessage.value = "${server.name} reboot completed!"
        }
    }

    fun toggleServerPower(server: ServerInstanceEntity) {
        viewModelScope.launch {
            val next = if (server.status == "RUNNING") "STOPPED" else "RUNNING"
            repository.updateServer(server.copy(status = next))
            appendTerminalLog("[POWER] ${server.name} ACPI signal -> $next")
            _userFeedbackMessage.value = "${server.name} is $next"
        }
    }

    fun addNewServer(
        name: String,
        ip: String,
        port: Int,
        region: String,
        osInfo: String,
        ramTotalMb: Int,
        diskTotalGb: Int
    ) {
        viewModelScope.launch {
            val newServer = ServerInstanceEntity(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "Cloud-VPS-${Random.nextInt(10, 99)}" },
                ipAddress = ip.ifBlank { "192.168.1.100" },
                sshPort = if (port > 0) port else 22,
                region = region.ifBlank { "us-east-1" },
                osInfo = osInfo.ifBlank { "Ubuntu 24.04 LTS" },
                status = "RUNNING",
                cpuPercent = Random.nextInt(15, 45).toFloat(),
                ramUsedMb = (ramTotalMb * 0.35f).toInt(),
                ramTotalMb = ramTotalMb,
                diskUsedGb = (diskTotalGb * 0.28f).toInt(),
                diskTotalGb = diskTotalGb,
                activeContainersCount = Random.nextInt(2, 6)
            )
            repository.insertServer(newServer)
            appendTerminalLog("[SERVER] Attached new instance ${newServer.name} (${newServer.ipAddress})")
            _userFeedbackMessage.value = "Server ${newServer.name} registered!"
        }
    }

    fun deleteServer(server: ServerInstanceEntity) {
        viewModelScope.launch {
            repository.deleteServer(server)
            appendTerminalLog("[SERVER] Removed instance ${server.name} from monitoring")
            _userFeedbackMessage.value = "Server removed"
        }
    }

    fun executeTerminalCommand(cmd: String) {
        viewModelScope.launch {
            appendTerminalLog("$ $cmd")
            when {
                cmd.trim().lowercase() == "clear" -> {
                    _terminalLogs.value = listOf("[$] Console cleared")
                }
                cmd.contains("docker ps") -> {
                    appendTerminalLog("CONTAINER ID   IMAGE                COMMAND                  STATUS         PORTS")
                    appendTerminalLog("a8f9c102b489   telegram-bot:latest  \"python3 shop_bot.py\"   Up 4 days      0.0.0.0:8080->8080")
                    appendTerminalLog("7b1409da21ee   node:20-alpine       \"node moderator.js\"      Up 2 days      0.0.0.0:3000->3000")
                }
                cmd.contains("top") || cmd.contains("htop") -> {
                    appendTerminalLog("Tasks: 148 total, 1 running, 147 sleeping")
                    appendTerminalLog("%Cpu(s):  3.4 us,  1.2 sy,  0.0 ni, 95.1 id,  0.2 wa")
                    appendTerminalLog("MiB Mem :   8192.0 total,   4210.5 used,   3981.5 free")
                }
                cmd.contains("ping") -> {
                    appendTerminalLog("PING api.telegram.org (149.154.167.220): 56 data bytes")
                    appendTerminalLog("64 bytes from 149.154.167.220: icmp_seq=0 ttl=54 time=38.4 ms")
                    appendTerminalLog("64 bytes from 149.154.167.220: icmp_seq=1 ttl=54 time=37.9 ms")
                }
                cmd.contains("systemctl restart") -> {
                    appendTerminalLog("[OK] Service restart completed successfully.")
                }
                else -> {
                    appendTerminalLog("[STDOUT] Executed command with exit status 0")
                }
            }
        }
    }

    private fun appendTerminalLog(line: String) {
        val current = _terminalLogs.value.takeLast(120).toMutableList()
        current.add(line)
        _terminalLogs.value = current
    }

    // API TASKS ACTIONS
    fun triggerTaskNow(task: ApiTaskEntity) {
        viewModelScope.launch {
            appendTerminalLog("[CRON] Manually executing task: ${task.name} (${task.endpointUrl})")
            delay(600)
            val isSuccess = Random.nextFloat() > 0.1f
            val statusCode = if (isSuccess) 200 else 500
            val latency = Random.nextInt(40, 180)
            repository.updateApiTask(task.copy(
                lastRunTimestamp = System.currentTimeMillis(),
                lastStatusCode = statusCode,
                lastLatencyMs = latency,
                status = if (isSuccess) "HEALTHY" else "FAILING"
            ))
            appendTerminalLog("[CRON] Task ${task.name} completed with HTTP $statusCode in ${latency}ms")
            _userFeedbackMessage.value = "Task executed: HTTP $statusCode (${latency}ms)"
        }
    }

    fun toggleTaskEnabled(task: ApiTaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(
                isEnabled = !task.isEnabled,
                status = if (!task.isEnabled) "HEALTHY" else "PAUSED"
            )
            repository.updateApiTask(updated)
            _userFeedbackMessage.value = "${task.name} is ${if (updated.isEnabled) "Enabled" else "Paused"}"
        }
    }

    fun addNewTask(
        name: String,
        url: String,
        cron: String,
        method: String
    ) {
        viewModelScope.launch {
            val newTask = ApiTaskEntity(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "Custom Poller Task" },
                endpointUrl = url.ifBlank { "https://api.internal/v1/ping" },
                cronExpression = cron.ifBlank { "*/5 * * * *" },
                method = method,
                status = "HEALTHY",
                lastLatencyMs = 65,
                successRatePercent = 100f,
                throughputReqMin = 60,
                lastRunTimestamp = System.currentTimeMillis(),
                lastStatusCode = 200,
                isEnabled = true
            )
            repository.insertApiTask(newTask)
            _userFeedbackMessage.value = "Automated Task ${newTask.name} scheduled!"
        }
    }

    fun deleteTask(task: ApiTaskEntity) {
        viewModelScope.launch {
            repository.deleteApiTask(task)
            _userFeedbackMessage.value = "Task deleted"
        }
    }

    // WEBHOOK ACTIONS
    fun testWebhook(webhook: WebhookConfigEntity, payloadOverride: String? = null) {
        viewModelScope.launch {
            val payload = payloadOverride ?: """{"event": "test.ping", "timestamp": ${System.currentTimeMillis()}, "source": "NexusControl Mobile Panel"}"""
            appendTerminalLog("[WEBHOOK] Dispatching test POST to ${webhook.targetUrl}")

            val (code, resBody) = repository.dispatchWebhookTest(webhook.targetUrl, payload)
            val isSuccess = code in 200..299

            val log = WebhookEventLogEntity(
                id = UUID.randomUUID().toString(),
                webhookId = webhook.id,
                eventType = "manual.test_dispatch",
                payloadJson = payload,
                responseCode = if (code > 0) code else 503,
                latencyMs = Random.nextInt(45, 120),
                timestamp = System.currentTimeMillis(),
                status = if (isSuccess) "SUCCESS" else "FAILED"
            )
            repository.insertWebhookLog(log)

            if (isSuccess) {
                repository.updateWebhook(webhook.copy(successCount = webhook.successCount + 1))
                _userFeedbackMessage.value = "Webhook delivered! HTTP $code"
            } else {
                repository.updateWebhook(webhook.copy(failedCount = webhook.failedCount + 1))
                _userFeedbackMessage.value = "Webhook dispatch failed (HTTP $code)"
            }
            appendTerminalLog("[WEBHOOK] Result: HTTP $code | Response: $resBody")
        }
    }

    fun toggleWebhookActive(webhook: WebhookConfigEntity) {
        viewModelScope.launch {
            val updated = webhook.copy(isActive = !webhook.isActive)
            repository.updateWebhook(updated)
            _userFeedbackMessage.value = "${webhook.name} is ${if (updated.isActive) "Active" else "Disabled"}"
        }
    }

    fun addNewWebhook(
        name: String,
        targetUrl: String,
        direction: String,
        eventFilters: String
    ) {
        viewModelScope.launch {
            val newWebhook = WebhookConfigEntity(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "Webhook Endpoint" },
                targetUrl = targetUrl.ifBlank { "https://hooks.external.io/events" },
                secretToken = "whsec_" + UUID.randomUUID().toString().replace("-", "").take(24),
                direction = direction,
                eventFilters = eventFilters.ifBlank { "bot.crash, user.grant_admin" },
                isActive = true,
                successCount = 0,
                failedCount = 0
            )
            repository.insertWebhook(newWebhook)
            _userFeedbackMessage.value = "Webhook ${newWebhook.name} created!"
        }
    }

    fun deleteWebhook(webhook: WebhookConfigEntity) {
        viewModelScope.launch {
            repository.deleteWebhook(webhook)
            _userFeedbackMessage.value = "Webhook removed"
        }
    }

    // ADMIN URL CONFIG
    fun regenerateAdminSecret() {
        viewModelScope.launch {
            val current = adminSettings.value ?: return@launch
            val newKey = "nxc_live_auth_" + UUID.randomUUID().toString().replace("-", "").take(20)
            val updated = current.copy(
                secretApiKey = newKey,
                lastSyncTimestamp = System.currentTimeMillis()
            )
            repository.saveAdminSettings(updated)
            appendTerminalLog("[ADMIN KEY] Regenerated Admin Bridge Token: $newKey")
            _userFeedbackMessage.value = "Admin Secret Key regenerated!"
        }
    }

    fun updateAllowedOrigins(origins: String) {
        viewModelScope.launch {
            val current = adminSettings.value ?: return@launch
            val updated = current.copy(
                allowedOrigins = origins,
                lastSyncTimestamp = System.currentTimeMillis()
            )
            repository.saveAdminSettings(updated)
            _userFeedbackMessage.value = "Allowed CORS Origins updated"
        }
    }

    // EMERGENCY CONTROLS
    fun emergencyPauseAllBots() {
        viewModelScope.launch {
            val currentBots = bots.value
            currentBots.forEach { bot ->
                if (bot.status == "RUNNING") {
                    repository.updateBot(bot.copy(status = "STOPPED"))
                }
            }
            appendTerminalLog("[EMERGENCY] Sent SIGSTOP to all active Telegram bots!")
            _userFeedbackMessage.value = "All bots paused"
        }
    }

    fun emergencyResumeAllBots() {
        viewModelScope.launch {
            val currentBots = bots.value
            currentBots.forEach { bot ->
                repository.updateBot(bot.copy(status = "RUNNING"))
            }
            appendTerminalLog("[EMERGENCY] Sent SIGCONT to all Telegram bots!")
            _userFeedbackMessage.value = "All bots resumed"
        }
    }
}
