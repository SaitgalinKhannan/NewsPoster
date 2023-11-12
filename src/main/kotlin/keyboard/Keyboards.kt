package keyboard

import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.utils.row

fun keyboardToPostNews() = inlineKeyboard {
    row { dataButton("⚡\uFE0F Опубликовать", "news") }
    row { dataButton("❌ Удалить", "deletenews") }
}

val keyboardToPostNewsForAlbum = inlineKeyboard {
    row { dataButton("⚡\uFE0F Опубликовать", "newsalbum") }
    row { dataButton("❌ Удалить", "deletenews") }
}

val keyboardToPostedNews = inlineKeyboard { row { dataButton("Опубликовано ⚡\uFE0F", "posted") } }

val adminKeyboard = replyKeyboard(
    resizeKeyboard = true, oneTimeKeyboard = true
) {
    row {
        simpleButton("Все чаты")
    }
    row {
        simpleButton("Добавить чат/канал")
        simpleButton("Удалить чат/канал")
    }
}