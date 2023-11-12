package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Admin(
    @SerialName("admins") val list: MutableSet<Long>
)
