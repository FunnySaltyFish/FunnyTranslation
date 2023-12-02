package com.funny.compose.ai.bean

import com.funny.translation.helper.JsonX
import kotlinx.serialization.Serializable

@Serializable
class ChatMessageReq(
    val role: String,
    val content: String
) {
    companion object {
        fun text(content: String, role: String = "user") = ChatMessageReq(role, content)
        fun vision(content: Vision, role: String = "user") = ChatMessageReq(role, JsonX.toJson(content))
    }

    /*
    "content": [
        {"type": "text", "text": "What’s in this image?"},
        {
          "type": "image_url",
          "image_url": {
            "url": "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg",
          },
        },
      ],
     */
    class Vision(
        val content: List<Content>,
    ) {
        @Serializable
        class Content(
            val type: String,
            val text: String? = null,
            val image_url: ImageUrl? = null,
        ) {
            @Serializable
            class ImageUrl(
                val url: String,
            )
        }
    }
}