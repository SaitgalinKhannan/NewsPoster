package model

import kotlinx.serialization.Serializable


@Serializable
data class Chat(
    val id: Long, val title: String = "", val username: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chat

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
