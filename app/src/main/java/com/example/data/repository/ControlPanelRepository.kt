package com.example.data.repository

import com.example.data.local.ControlPanelDao
import com.example.data.model.*
import com.example.data.network.TelegramApiClient
import com.example.data.network.TelegramBotCheckResult
import kotlinx.coroutines.flow.Flow

class ControlPanelRepository(
    private val dao: ControlPanelDao,
    private val apiClient: TelegramApiClient = TelegramApiClient()
) {
    // Bots
    val allBots: Flow<List<BotEntity>> = dao.getAllBots()
    fun getBot(botId: String): Flow<BotEntity?> = dao.getBotById(botId)

    suspend fun insertBot(bot: BotEntity) = dao.insertBot(bot)
    suspend fun updateBot(bot: BotEntity) = dao.updateBot(bot)
    suspend fun deleteBot(bot: BotEntity) = dao.deleteBot(bot)

    // Bot Users
    fun getBotUsers(botId: String): Flow<List<BotUserEntity>> = dao.getBotUsers(botId)
    val allUsers: Flow<List<BotUserEntity>> = dao.getAllBotUsers()

    suspend fun insertBotUser(user: BotUserEntity) = dao.insertBotUser(user)
    suspend fun updateBotUser(user: BotUserEntity) = dao.updateBotUser(user)
    suspend fun deleteBotUser(user: BotUserEntity) = dao.deleteBotUser(user)

    // Servers
    val allServers: Flow<List<ServerInstanceEntity>> = dao.getAllServers()
    suspend fun insertServer(server: ServerInstanceEntity) = dao.insertServer(server)
    suspend fun updateServer(server: ServerInstanceEntity) = dao.updateServer(server)
    suspend fun deleteServer(server: ServerInstanceEntity) = dao.deleteServer(server)

    // Tasks
    val allTasks: Flow<List<ApiTaskEntity>> = dao.getAllApiTasks()
    suspend fun insertApiTask(task: ApiTaskEntity) = dao.insertApiTask(task)
    suspend fun updateApiTask(task: ApiTaskEntity) = dao.updateApiTask(task)
    suspend fun deleteApiTask(task: ApiTaskEntity) = dao.deleteApiTask(task)

    // Webhooks
    val allWebhooks: Flow<List<WebhookConfigEntity>> = dao.getAllWebhooks()
    val recentWebhookLogs: Flow<List<WebhookEventLogEntity>> = dao.getRecentWebhookLogs()
    suspend fun insertWebhook(webhook: WebhookConfigEntity) = dao.insertWebhook(webhook)
    suspend fun updateWebhook(webhook: WebhookConfigEntity) = dao.updateWebhook(webhook)
    suspend fun deleteWebhook(webhook: WebhookConfigEntity) = dao.deleteWebhook(webhook)
    suspend fun insertWebhookLog(log: WebhookEventLogEntity) = dao.insertWebhookLog(log)

    // Admin Settings
    val adminSettings: Flow<AdminSettingsEntity?> = dao.getAdminSettings()
    suspend fun saveAdminSettings(settings: AdminSettingsEntity) = dao.saveAdminSettings(settings)

    // Network Actions
    suspend fun verifyTelegramToken(token: String): TelegramBotCheckResult {
        return apiClient.verifyBotToken(token)
    }

    suspend fun dispatchWebhookTest(url: String, payloadJson: String): Pair<Int, String> {
        return apiClient.dispatchWebhookTest(url, payloadJson)
    }
}
