package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val token: String,
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("chat_for_news")
    val chatForNews: Long,
    @SerialName("userbot_id")
    val userBotId: Long
)
