package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        BotEntity::class,
        BotUserEntity::class,
        ServerInstanceEntity::class,
        ApiTaskEntity::class,
        WebhookConfigEntity::class,
        WebhookEventLogEntity::class,
        AdminSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun controlPanelDao(): ControlPanelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexus_control_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.controlPanelDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: ControlPanelDao) {
            val bot1Id = "bot-ecom-01"
            val bot2Id = "bot-moderator-02"
            val bot3Id = "bot-signals-03"

            // Seed Initial Bots
            val bots = listOf(
                BotEntity(
                    id = bot1Id,
                    name = "OmniStore Commerce Bot",
                    username = "@OmniStoreDirectBot",
                    token = "6829104821:AAHzw99xKLP4m-kX_vJ7qEw209A-40x8QW1",
                    scriptFile = "services/telegram/shop_bot.py",
                    status = "RUNNING",
                    uptimeSeconds = 348200L,
                    activeUsersCount = 1420,
                    totalCommandsProcessed = 89421L,
                    lastPingMs = 38,
                    hostServerName = "US-East-Prod-01",
                    createdAt = System.currentTimeMillis() - 864000000L
                ),
                BotEntity(
                    id = bot2Id,
                    name = "Sentinel AI Community Guard",
                    username = "@SentinelGuardAIBot",
                    token = "7193821094:BBkQ_01jM994Lm-zR82kLAw931kLmQW7",
                    scriptFile = "dist/moderator_daemon.js",
                    status = "RUNNING",
                    uptimeSeconds = 189200L,
                    activeUsersCount = 5820,
                    totalCommandsProcessed = 249102L,
                    lastPingMs = 45,
                    hostServerName = "EU-Central-Node-02",
                    createdAt = System.currentTimeMillis() - 432000000L
                ),
                BotEntity(
                    id = bot3Id,
                    name = "AlphaSignals Broadcast Bot",
                    username = "@AlphaSignalsLiveBot",
                    token = "5820491823:CCpL_9401vNxR-pM710kDA9402LmQW9",
                    scriptFile = "workers/signals_dispatcher.go",
                    status = "IDLE",
                    uptimeSeconds = 48100L,
                    activeUsersCount = 890,
                    totalCommandsProcessed = 15340L,
                    lastPingMs = 62,
                    hostServerName = "Asia-Tokyo-Node-01",
                    createdAt = System.currentTimeMillis() - 172800000L
                )
            )
            bots.forEach { dao.insertBot(it) }

            // Seed Bot Users with Granular Permissions
            val botUsers = listOf(
                BotUserEntity(
                    id = UUID.randomUUID().toString(),
                    botId = bot1Id,
                    telegramUserId = 71829401L,
                    username = "@alex_founder",
                    displayName = "Alex Carter (Owner)",
                    role = "OWNER",
                    canUseCommands = true,
                    canBroadcast = true,
                    hasAdminAccess = true,
                    isPremium = true,
                    canExecuteScripts = true,
                    canManageUsers = true,
                    joinedAt = System.currentTimeMillis() - 800000000L,
                    lastActiveAt = System.currentTimeMillis() - 120000L,
                    totalMessagesSent = 1540
                ),
                BotUserEntity(
                    id = UUID.randomUUID().toString(),
                    botId = bot1Id,
                    telegramUserId = 89201948L,
                    username = "@sarah_ops",
                    displayName = "Sarah Jenkins",
                    role = "ADMIN",
                    canUseCommands = true,
                    canBroadcast = true,
                    hasAdminAccess = true,
                    isPremium = true,
                    canExecuteScripts = false,
                    canManageUsers = true,
                    joinedAt = System.currentTimeMillis() - 600000000L,
                    lastActiveAt = System.currentTimeMillis() - 3400000L,
                    totalMessagesSent = 842
                ),
                BotUserEntity(
                    id = UUID.randomUUID().toString(),
                    botId = bot1Id,
                    telegramUserId = 94810294L,
                    username = "@marcus_vip",
                    displayName = "Marcus Vance",
                    role = "VIP",
                    canUseCommands = true,
                    canBroadcast = false,
                    hasAdminAccess = false,
                    isPremium = true,
                    canExecuteScripts = false,
                    canManageUsers = false,
                    joinedAt = System.currentTimeMillis() - 250000000L,
                    lastActiveAt = System.currentTimeMillis() - 7200000L,
                    totalMessagesSent = 320
                ),
                BotUserEntity(
                    id = UUID.randomUUID().toString(),
                    botId = bot1Id,
                    telegramUserId = 10482910L,
                    username = "@spambot_user",
                    displayName = "Promo Account 49",
                    role = "BANNED",
                    canUseCommands = false,
                    canBroadcast = false,
                    hasAdminAccess = false,
                    isPremium = false,
                    canExecuteScripts = false,
                    canManageUsers = false,
                    joinedAt = System.currentTimeMillis() - 100000000L,
                    lastActiveAt = System.currentTimeMillis() - 86400000L,
                    totalMessagesSent = 12
                ),
                BotUserEntity(
                    id = UUID.randomUUID().toString(),
                    botId = bot2Id,
                    telegramUserId = 64920194L,
                    username = "@elena_mod",
                    displayName = "Elena Rostova",
                    role = "MODERATOR",
                    canUseCommands = true,
                    canBroadcast = true,
                    hasAdminAccess = false,
                    isPremium = true,
                    canExecuteScripts = false,
                    canManageUsers = true,
                    joinedAt = System.currentTimeMillis() - 400000000L,
                    lastActiveAt = System.currentTimeMillis() - 900000L,
                    totalMessagesSent = 1120
                )
            )
            botUsers.forEach { dao.insertBotUser(it) }

            // Seed Server Instances
            val servers = listOf(
                ServerInstanceEntity(
                    id = "srv-us-east-01",
                    name = "US-East-Prod-01",
                    ipAddress = "198.51.100.24",
                    sshPort = 2222,
                    region = "us-east-1 (N. Virginia)",
                    osInfo = "Ubuntu 24.04 LTS (Kernel 6.8.0)",
                    status = "RUNNING",
                    cpuPercent = 34.2f,
                    ramUsedMb = 3680,
                    ramTotalMb = 8192,
                    diskUsedGb = 42,
                    diskTotalGb = 160,
                    activeContainersCount = 7,
                    lastChecked = System.currentTimeMillis()
                ),
                ServerInstanceEntity(
                    id = "srv-eu-central-02",
                    name = "EU-Central-Node-02",
                    ipAddress = "203.0.113.88",
                    sshPort = 22,
                    region = "eu-central (Frankfurt)",
                    osInfo = "Debian 12 Bookworm",
                    status = "RUNNING",
                    cpuPercent = 58.7f,
                    ramUsedMb = 7120,
                    ramTotalMb = 16384,
                    diskUsedGb = 98,
                    diskTotalGb = 320,
                    activeContainersCount = 14,
                    lastChecked = System.currentTimeMillis()
                ),
                ServerInstanceEntity(
                    id = "srv-ap-tokyo-01",
                    name = "Asia-Tokyo-Node-01",
                    ipAddress = "192.0.2.145",
                    sshPort = 22,
                    region = "ap-northeast-1 (Tokyo)",
                    osInfo = "Alpine Linux 3.20 (Edge)",
                    status = "RUNNING",
                    cpuPercent = 14.8f,
                    ramUsedMb = 1420,
                    ramTotalMb = 4096,
                    diskUsedGb = 18,
                    diskTotalGb = 80,
                    activeContainersCount = 3,
                    lastChecked = System.currentTimeMillis()
                )
            )
            servers.forEach { dao.insertServer(it) }

            // Seed Automated API Tasks
            val tasks = listOf(
                ApiTaskEntity(
                    id = "task-sync-tg",
                    name = "Telegram User & Token Sync",
                    endpointUrl = "https://api.internal.nexus/v1/telegram/sync-sessions",
                    cronExpression = "*/2 * * * *",
                    method = "POST",
                    status = "HEALTHY",
                    lastLatencyMs = 64,
                    successRatePercent = 99.8f,
                    throughputReqMin = 420,
                    lastRunTimestamp = System.currentTimeMillis() - 45000L,
                    lastStatusCode = 200,
                    isEnabled = true
                ),
                ApiTaskEntity(
                    id = "task-sub-renewal",
                    name = "Subscription & Invoicing Poller",
                    endpointUrl = "https://api.internal.nexus/v1/billing/auto-verify",
                    cronExpression = "0 */1 * * *",
                    method = "GET",
                    status = "HEALTHY",
                    lastLatencyMs = 142,
                    successRatePercent = 98.4f,
                    throughputReqMin = 85,
                    lastRunTimestamp = System.currentTimeMillis() - 1800000L,
                    lastStatusCode = 200,
                    isEnabled = true
                ),
                ApiTaskEntity(
                    id = "task-backup-db",
                    name = "Nightly S3 Encrypted Backup",
                    endpointUrl = "https://api.internal.nexus/v1/storage/backup-snapshot",
                    cronExpression = "0 3 * * *",
                    method = "POST",
                    status = "HEALTHY",
                    lastLatencyMs = 890,
                    successRatePercent = 100.0f,
                    throughputReqMin = 1,
                    lastRunTimestamp = System.currentTimeMillis() - 28000000L,
                    lastStatusCode = 200,
                    isEnabled = true
                ),
                ApiTaskEntity(
                    id = "task-rate-limiter",
                    name = "DDoS & Rate Limit Sweeper",
                    endpointUrl = "https://api.internal.nexus/v1/sec/flush-quarantine",
                    cronExpression = "*/5 * * * *",
                    method = "POST",
                    status = "DEGRADED",
                    lastLatencyMs = 380,
                    successRatePercent = 91.2f,
                    throughputReqMin = 120,
                    lastRunTimestamp = System.currentTimeMillis() - 110000L,
                    lastStatusCode = 429,
                    isEnabled = true
                )
            )
            tasks.forEach { dao.insertApiTask(it) }

            // Seed Webhooks
            val webhooks = listOf(
                WebhookConfigEntity(
                    id = "wh-discord-alerts",
                    name = "Discord Ops Channel Alerts",
                    targetUrl = "https://discord.com/api/webhooks/120938/alert-stream",
                    secretToken = "whsec_live_99fa0218bca489e",
                    direction = "OUTGOING",
                    eventFilters = "bot.crash, server.high_cpu, task.failed",
                    isActive = true,
                    successCount = 1842,
                    failedCount = 3,
                    createdAt = System.currentTimeMillis() - 800000000L
                ),
                WebhookConfigEntity(
                    id = "wh-website-admin",
                    name = "Website Admin Remote Ingress",
                    targetUrl = "https://hooks.nexuscontrol.io/ingress/web-admin-events",
                    secretToken = "whsec_ing_4488bfa00921aae",
                    direction = "INCOMING",
                    eventFilters = "admin.user_update, order.created, bot.permission_change",
                    isActive = true,
                    successCount = 9481,
                    failedCount = 0,
                    createdAt = System.currentTimeMillis() - 600000000L
                )
            )
            webhooks.forEach { dao.insertWebhook(it) }

            // Seed Webhook Event Logs
            val logs = listOf(
                WebhookEventLogEntity(
                    id = UUID.randomUUID().toString(),
                    webhookId = "wh-discord-alerts",
                    eventType = "server.health_check",
                    payloadJson = """{"server": "US-East-Prod-01", "cpu": 34.2, "status": "HEALTHY"}""",
                    responseCode = 200,
                    latencyMs = 82,
                    timestamp = System.currentTimeMillis() - 15000L,
                    status = "SUCCESS"
                ),
                WebhookEventLogEntity(
                    id = UUID.randomUUID().toString(),
                    webhookId = "wh-website-admin",
                    eventType = "bot.permission_change",
                    payloadJson = """{"user": "@alex_founder", "role": "OWNER", "botId": "bot-ecom-01"}""",
                    responseCode = 200,
                    latencyMs = 45,
                    timestamp = System.currentTimeMillis() - 120000L,
                    status = "SUCCESS"
                ),
                WebhookEventLogEntity(
                    id = UUID.randomUUID().toString(),
                    webhookId = "wh-discord-alerts",
                    eventType = "task.rate_limit_warning",
                    payloadJson = """{"task": "DDoS Sweeper", "http_code": 429, "retry_after": 60}""",
                    responseCode = 429,
                    latencyMs = 210,
                    timestamp = System.currentTimeMillis() - 360000L,
                    status = "FAILED"
                )
            )
            logs.forEach { dao.insertWebhookLog(it) }

            // Seed Admin Bridge Settings
            val adminSettings = AdminSettingsEntity(
                id = 1,
                adminBridgeUrl = "https://api.nexuscontrol.io/v1/admin/bridge?node=live_gateway_primary",
                secretApiKey = "nxc_live_auth_88f921ab9403efc91",
                allowedOrigins = "https://yourwebsite.com, https://admin.yourdomain.org, http://localhost:3000",
                isBridgeActive = true,
                lastSyncTimestamp = System.currentTimeMillis()
            )
            dao.saveAdminSettings(adminSettings)
        }
    }
}
