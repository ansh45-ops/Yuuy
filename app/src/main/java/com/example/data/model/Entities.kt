package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bots")
data class BotEntity(
    @PrimaryKey val id: String,
    val name: String,
    val username: String,
    val token: String,
    val scriptFile: String,
    val status: String, // RUNNING, STOPPED, ERROR, IDLE
    val uptimeSeconds: Long,
    val activeUsersCount: Int,
    val totalCommandsProcessed: Long,
    val lastPingMs: Int,
    val hostServerName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bot_users")
data class BotUserEntity(
    @PrimaryKey val id: String,
    val botId: String,
    val telegramUserId: Long,
    val username: String,
    val displayName: String,
    val role: String, // OWNER, ADMIN, MODERATOR, VIP, USER, BANNED
    val canUseCommands: Boolean,
    val canBroadcast: Boolean,
    val hasAdminAccess: Boolean,
    val isPremium: Boolean,
    val canExecuteScripts: Boolean,
    val canManageUsers: Boolean,
    val joinedAt: Long,
    val lastActiveAt: Long,
    val totalMessagesSent: Int = 42
)

@Entity(tableName = "server_instances")
data class ServerInstanceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val ipAddress: String,
    val sshPort: Int,
    val region: String,
    val osInfo: String,
    val status: String, // RUNNING, STOPPED, REBOOTING, ALERT
    val cpuPercent: Float,
    val ramUsedMb: Int,
    val ramTotalMb: Int,
    val diskUsedGb: Int,
    val diskTotalGb: Int,
    val activeContainersCount: Int,
    val lastChecked: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_tasks")
data class ApiTaskEntity(
    @PrimaryKey val id: String,
    val name: String,
    val endpointUrl: String,
    val cronExpression: String,
    val method: String, // GET, POST, PUT, DELETE
    val status: String, // HEALTHY, DEGRADED, FAILING, PAUSED
    val lastLatencyMs: Int,
    val successRatePercent: Float,
    val throughputReqMin: Int,
    val lastRunTimestamp: Long,
    val lastStatusCode: Int,
    val isEnabled: Boolean
)

@Entity(tableName = "webhook_configs")
data class WebhookConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val targetUrl: String,
    val secretToken: String,
    val direction: String, // OUTGOING, INCOMING
    val eventFilters: String, // comma separated
    val isActive: Boolean,
    val successCount: Int,
    val failedCount: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "webhook_logs")
data class WebhookEventLogEntity(
    @PrimaryKey val id: String,
    val webhookId: String,
    val eventType: String,
    val payloadJson: String,
    val responseCode: Int,
    val latencyMs: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String // SUCCESS, FAILED, RETRYING
)

@Entity(tableName = "admin_settings")
data class AdminSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val adminBridgeUrl: String,
    val secretApiKey: String,
    val allowedOrigins: String,
    val isBridgeActive: Boolean,
    val lastSyncTimestamp: Long
)
