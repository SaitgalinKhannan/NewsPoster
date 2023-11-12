package utils

import dev.inmo.micro_utils.fsm.common.managers.DefaultStatesManager
import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.edit.reply_markup.editMessageReplyMarkup
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.DefaultBehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextedContentMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.message
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.reply_to_message
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MediaGroupContent
import dev.inmo.tgbotapi.types.message.content.VisualMediaGroupPartContent
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.RiskFeature
import io.allChats
import keyboard.keyboardToPostedNews
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import model.Admin
import model.Config
import state.AddChatState
import state.BotState
import state.DeleteChatState
import text.Text.Companion.chatsToTextList
import java.io.EOFException
import java.io.File

fun adminList(adminJsonFile: File): Admin {
    return try {
        Json.decodeFromString<Admin>(adminJsonFile.readText())
    } catch (e: EOFException) {
        println(e.message)
        Admin(mutableSetOf())
    }
}

@OptIn(RiskFeature::class)
suspend inline fun DefaultBehaviourContextWithFSM<BotState>.adminUtils(
    messages: MutableMap<Long, CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>>,
    stateManager: DefaultStatesManager<BotState>,
    config: Config
) {
    onDataCallbackQuery {
        when {
            it.data == "news" -> {
                try {
                    answerCallbackQuery(it)
                    if (it.message != null) {
                        execute(it.message!!.content.createResend(chatId = config.chatForNews.toChatId()))
                        editMessageReplyMarkup(
                            chatId = it.message!!.chat.id,
                            messageId = it.message!!.messageId,
                            replyMarkup = keyboardToPostedNews
                        )
                    }
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            it.data.contains("deletenews") -> {
                try {
                    answerCallbackQuery(it)
                    if (it.message != null) {
                        it.message!!.reply_to_message?.let { replyToMessage -> deleteMessage(replyToMessage) }
                        deleteMessage(it.message!!)
                    }
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            it.data == "newsalbum" -> {
                try {
                    answerCallbackQuery(it)
                    if (it.message != null) {
                        val message = messages[it.message!!.reply_to_message?.messageId]
                        messages.remove(it.message!!.reply_to_message?.messageId)
                        if (message != null) {
                            execute(message.content.createResend(chatId = config.chatForNews.toChatId()))
                        }
                        editMessageReplyMarkup(
                            chatId = it.message!!.chat.id,
                            messageId = it.message!!.messageId,
                            replyMarkup = keyboardToPostedNews
                        )
                    }
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            it.data.contains("deletenewsalbum") -> {
                try {
                    answerCallbackQuery(it)
                    if (it.message != null) {
                        it.message!!.reply_to_message?.let { replyToMessage -> deleteMessage(replyToMessage) }
                        deleteMessage(it.message!!)
                    }
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            it.data == "posted" -> {
                answerCallbackQuery(callbackQuery = it, text = "Запись уже опубликована!", showAlert = false)
            }
        }
    }

    onText {
        when (it.text) {
            "Добавить чат/канал" -> {
                try {
                    try {
                        val userState =
                            stateManager.getActiveStates().first { state: BotState -> state.context == it.chat.id }
                        stateManager.endChain(userState)
                    } catch (e: Exception) {
                        println(e.message)
                    }

                    sendMessage(
                        chat = it.chat,
                        text = "Введите username или ссылку, чтобы добавить чат!"
                    )

                    startChain(
                        AddChatState(
                            context = it.chat.id,
                            idMessageIdentifier = it.messageId
                        )
                    )
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            "Удалить чат/канал" -> {
                try {
                    try {
                        val userState =
                            stateManager.getActiveStates().first { state: BotState -> state.context == it.chat.id }
                        stateManager.endChain(userState)
                    } catch (e: Exception) {
                        println(e.message)
                    }

                    sendMessage(
                        chat = it.chat,
                        text = "Введите id или username, чтобы удалить чат!"
                    )

                    startChain(
                        DeleteChatState(
                            context = it.chat.id,
                            idMessageIdentifier = it.messageId
                        )
                    )
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            "Все чаты" -> {
                try {
                    val chats = allChats()
                    sendMessage(
                        chat = it.chat,
                        entities = chatsToTextList(chats)
                    )
                } catch (e: Exception) {
                    sendMessage(
                        chat = it.chat,
                        text = e.message.toString()
                    )
                    println(e.message)
                }
            }
        }
    }

    strictlyOn<AddChatState> {
        waitTextedContentMessage().filter { commonMessage -> commonMessage.text != "Все чаты" && commonMessage.text != "/start" }
            .map { commonMessage ->
                try {
                    sendMessage(
                        chatId = config.userBotId.toChatId(),
                        text = "/botaddchat ${commonMessage.text} ID:${it.context.chatId}"
                    )
                } catch (e: Exception) {
                    sendMessage(
                        chat = commonMessage.chat,
                        text = e.message.toString()
                    )
                    println(e.message)
                }
                null
            }.first()
    }

    strictlyOn<DeleteChatState> {
        waitTextedContentMessage().filter { commonMessage -> commonMessage.text != "Все чаты" && commonMessage.text != "/start" }
            .map { commonMessage ->
                try {
                    sendMessage(
                        chatId = config.userBotId.toChatId(),
                        text = "/botremovechat ${commonMessage.text} ID:${it.context.chatId}"
                    )
                } catch (e: Exception) {
                    sendMessage(
                        chat = commonMessage.chat,
                        text = e.message.toString()
                    )
                    println(e.message)
                }
                null
            }.first()
    }
}