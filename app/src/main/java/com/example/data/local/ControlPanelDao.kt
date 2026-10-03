package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ControlPanelDao {

    // Bots
    @Query("SELECT * FROM bots ORDER BY createdAt DESC")
    fun getAllBots(): Flow<List<BotEntity>>

    @Query("SELECT * FROM bots WHERE id = :botId")
    fun getBotById(botId: String): Flow<BotEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBot(bot: BotEntity)

    @Update
    suspend fun updateBot(bot: BotEntity)

    @Delete
    suspend fun deleteBot(bot: BotEntity)

    // Bot Users
    @Query("SELECT * FROM bot_users WHERE botId = :botId ORDER BY lastActiveAt DESC")
    fun getBotUsers(botId: String): Flow<List<BotUserEntity>>

    @Query("SELECT * FROM bot_users ORDER BY lastActiveAt DESC")
    fun getAllBotUsers(): Flow<List<BotUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBotUser(user: BotUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBotUsers(users: List<BotUserEntity>)

    @Update
    suspend fun updateBotUser(user: BotUserEntity)

    @Delete
    suspend fun deleteBotUser(user: BotUserEntity)

    // Servers
    @Query("SELECT * FROM server_instances ORDER BY name ASC")
    fun getAllServers(): Flow<List<ServerInstanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerInstanceEntity)

    @Update
    suspend fun updateServer(server: ServerInstanceEntity)

    @Delete
    suspend fun deleteServer(server: ServerInstanceEntity)

    // API Tasks
    @Query("SELECT * FROM api_tasks ORDER BY name ASC")
    fun getAllApiTasks(): Flow<List<ApiTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiTask(task: ApiTaskEntity)

    @Update
    suspend fun updateApiTask(task: ApiTaskEntity)

    @Delete
    suspend fun deleteApiTask(task: ApiTaskEntity)

    // Webhooks
    @Query("SELECT * FROM webhook_configs ORDER BY createdAt DESC")
    fun getAllWebhooks(): Flow<List<WebhookConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebhook(webhook: WebhookConfigEntity)

    @Update
    suspend fun updateWebhook(webhook: WebhookConfigEntity)

    @Delete
    suspend fun deleteWebhook(webhook: WebhookConfigEntity)

    // Webhook Event Logs
    @Query("SELECT * FROM webhook_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentWebhookLogs(): Flow<List<WebhookEventLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebhookLog(log: WebhookEventLogEntity)

    // Admin Settings
    @Query("SELECT * FROM admin_settings WHERE id = 1 LIMIT 1")
    fun getAdminSettings(): Flow<AdminSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdminSettings(settings: AdminSettingsEntity)
}
