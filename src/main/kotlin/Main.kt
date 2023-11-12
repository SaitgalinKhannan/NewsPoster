import dev.inmo.micro_utils.fsm.common.managers.DefaultStatesManager
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithFSMAndStartLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.*
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.MediaGroupContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.VideoContent
import dev.inmo.tgbotapi.types.message.content.VisualMediaGroupPartContent
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.RiskFeature
import dev.inmo.tgbotapi.utils.buildEntities
import keyboard.adminKeyboard
import keyboard.keyboardToPostNews
import keyboard.keyboardToPostNewsForAlbum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import model.Admin
import model.Config
import state.AddChatState
import state.BotState
import state.DeleteChatState
import text.Text.Companion.result
import utils.adminList
import utils.adminUtils
import java.io.File

@OptIn(RiskFeature::class)
suspend fun main(args: Array<String>) {
    val config = Json.decodeFromString<Config>(File(args.first()).readText())
    //val config = Json.decodeFromString<Config>(File("C:\\KProjects\\NewsPoster\\build\\libs\\config.json").readText())
    val mainChatId = config.chatId
    val userBotId = config.userBotId
    val bot = telegramBot(config.token)
    val messages = mutableMapOf<Long, CommonMessage<MediaGroupContent<VisualMediaGroupPartContent>>>()
    val videoMessages = mutableMapOf<Long, CommonMessage<MediaGroupContent<VideoContent>>>()
    val stateManager = DefaultStatesManager<BotState>()
    val adminJsonFile = File("admins.json")
    //val adminJsonFile = File("C:\\KProjects\\NewsPoster\\build\\libs\\admins.json")
    var admins = adminList(adminJsonFile)
    println(bot.getMe())

    bot.buildBehaviourWithFSMAndStartLongPolling(scope = CoroutineScope(Dispatchers.IO),
        statesManager = stateManager,
        onStateHandlingErrorHandler = { state, e ->
            when (state) {
                is AddChatState -> {
                    println("Thrown error on AddChatState")
                }

                is DeleteChatState -> {
                    println("Thrown error on DeleteChatState")
                }
            }
            e.printStackTrace()
            state
        }) {
        setMyCommands(listOf(BotCommand("start", "Старт")))
        adminUtils(messages, stateManager, config)

        fun String.isLong(): Boolean {
            return this.toLongOrNull() != null
        }

        onCommandWithArgs("rnt") { commonMessage: CommonMessage<TextContent>, strings: Array<String> ->
            admins = adminList(adminJsonFile)
            if (admins.list.contains(commonMessage.chat.id.chatId) && !admins.list.contains(strings.first().toLong())) {
                if (strings.first().isLong()) {
                    admins.list.add(strings.first().toLong())
                }
                adminJsonFile.writeText(Json.encodeToJsonElement<Admin>(admins).toString())
                sendMessage(
                    chat = commonMessage.chat,
                    text = "Теперь User = ${strings.first()} администратор",
                    replyMarkup = adminKeyboard
                )
            } else if (admins.list.contains(commonMessage.chat.id.chatId) && admins.list.contains(
                    strings.first().toLong()
                )
            ) {
                sendMessage(
                    chat = commonMessage.chat,
                    text = "User = ${strings.first()} уже администратор",
                    replyMarkup = adminKeyboard
                )
            }
        }

        onCommand("start") {
            try {
                val userState =
                    stateManager.getActiveStates().first { state: BotState -> state.context == it.chat.id }
                stateManager.endChain(userState)
            } catch (e: Exception) {
                println(e.message)
            }
            if (admins.list.contains(it.chat.id.chatId)) {
                sendMessage(
                    chat = it.chat,
                    text = "Вы администратор \uD83D\uDC51",
                    replyMarkup = adminKeyboard
                )
            }
        }

        onVideoGalleryMessages {
            try {
                if (it.chat.id.chatId != mainChatId && it.chat.id.chatId == userBotId && !admins.list.contains(it.chat.id.chatId)) {
                    val message = execute(
                        it.content.createResend(
                            chatId = mainChatId.toChatId()
                        )
                    )
                    videoMessages[message.messageId] = it
                    reply(
                        to = message,
                        entities = buildEntities {
                            +"\n\nОпубликовать предыдущее сообщение?"
                        },
                        replyMarkup = keyboardToPostNewsForAlbum
                    )
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }

        onVisualGalleryMessages {
            try {
                if (it.chat.id.chatId != mainChatId && it.chat.id.chatId == userBotId && !admins.list.contains(it.chat.id.chatId)) {
                    val message = execute(
                        it.content.createResend(
                            chatId = mainChatId.toChatId()
                        )
                    )
                    messages[message.messageId] = it
                    reply(
                        to = message,
                        text = "\n\nОпубликовать предыдущее сообщение?",
                        replyMarkup = keyboardToPostNewsForAlbum
                    )
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }

        onVideo {
            try {
                if (it.chat.id.chatId != mainChatId && it.chat.id.chatId == userBotId && !admins.list.contains(it.chat.id.chatId)) {
                    execute(
                        it.content.createResend(
                            chatId = mainChatId.toChatId(),
                            replyMarkup = keyboardToPostNews()
                        )
                    )
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }

        onPhoto {
            try {
                if (it.chat.id.chatId != mainChatId && it.chat.id.chatId == userBotId && !admins.list.contains(it.chat.id.chatId)) {
                    execute(
                        it.content.createResend(
                            chatId = mainChatId.toChatId(),
                            replyMarkup = keyboardToPostNews()
                        )
                    )
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }

        onText {
            try {
                when {
                    result.contains(it.text?.substringBefore(" ID")) -> {
                        val message = it.text?.substringBefore(" ID")
                        val id = it.text?.substringAfter("ID:")?.toInt()

                        if (message != null && id != null) {
                            sendMessage(
                                chatId = id.toChatId(),
                                text = message
                            )
                        }
                    }

                    it.chat.id.chatId != mainChatId && it.chat.id.chatId == userBotId && !admins.list.contains(it.chat.id.chatId) -> {
                        execute(
                            it.content.createResend(
                                chatId = mainChatId.toChatId(),
                                replyMarkup = keyboardToPostNews()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }.join()
}