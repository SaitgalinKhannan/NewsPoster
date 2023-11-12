package io

import kotlinx.serialization.json.Json
import model.Chat
import java.io.File

private val json = Json { coerceInputValues = true }

fun allChats(): List<Chat> {
    val fileWithChats = File("/root/lex_resender/chats.json")
    return try {
        json.decodeFromString<List<Chat>>(fileWithChats.readText())
    } catch (e: Exception) {
        throw Exception(e.message)
    }
}