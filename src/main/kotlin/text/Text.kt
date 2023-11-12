package text

import dev.inmo.tgbotapi.types.message.textsources.TextSourcesList
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.code
import model.Chat


class Text {
    companion object {
        fun chatsToTextList(chats: List<Chat>): TextSourcesList {
            return buildEntities {
                repeat(chats.size) {
                    +"ID: " + code(chats[it].id.toString())
                    +"\nНазвание: ${chats[it].title}\n"
                    +"Ссылка: https://t.me/${chats[it].username.substringAfter("@")}\n\n"
                }

                if (chats.isEmpty()) {
                    +"На данный момент чатов/каналов нет. \uD83D\uDD95"
                }
            }
        }

        val result = listOf(
            "Чата с таким id нет в списке",
            "Чата с таким username нет в списке",
            "Чат с таким id уже есть в списке",
            "Чат с таким username уже есть в списке",
            "Чат успешно добавлен",
            "Чат был успешно удален из списка"
        )
    }
}