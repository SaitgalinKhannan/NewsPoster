package state

import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.MessageId

sealed interface BotState : State

data class AddChatState(
    override val context: IdChatIdentifier,
    val idMessageIdentifier: MessageId
) : BotState

data class DeleteChatState(
    override val context: IdChatIdentifier,
    val idMessageIdentifier: MessageId
) : BotState
