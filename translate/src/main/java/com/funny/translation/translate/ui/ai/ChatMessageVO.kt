package com.funny.translation.translate.ui.ai

import androidx.compose.runtime.Stable
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageTypes

// 用于页面显示的聊天消息
// VO: View Object
@Stable
sealed class ChatMessageVO(
    val sender: String,
    val avatar: String? = null,
    val responding: Boolean = true
) {
    class Text(
        sender: String,
        val content: String,
        avatar: String? = null,
        responding: Boolean = true
    ) : ChatMessageVO(sender, avatar, responding)

    class Image(
        sender: String,
        val url: String,
        avatar: String? = null,
        responding: Boolean = true
    ) : ChatMessageVO(sender, avatar, responding)

    class Audio(
        sender: String,
        val url: String,
        avatar: String? = null,
        responding: Boolean = true
    ) : ChatMessageVO(sender, avatar, responding)
}

fun ChatMessageVO.fromChatMessage(chatMessage: ChatMessage) = when (chatMessage.type) {
    ChatMessageTypes.TEXT -> ChatMessageVO.Text(
        chatMessage.sender,
        chatMessage.content,
        responding = chatMessage.sendByMe
    )
    ChatMessageTypes.IMAGE -> ChatMessageVO.Image(
        chatMessage.sender,
        chatMessage.content,
        responding = chatMessage.sendByMe
    )
    else -> ChatMessageVO.Text(
        chatMessage.sender,
        chatMessage.content,
        responding = chatMessage.sendByMe
    )
}
