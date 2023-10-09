package com.funny.compose.ai

import com.funny.compose.ai.bean.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.json.JSONObject

const val LONG_TEXT_TRANS_PROMPT = """你现在是一名优秀的翻译人员，现在在翻译长文中的某个片段。请根据给定的术语表，将输入文本翻译成中文，并找出可能存在的新术语，以JSON的形式返回（如果没有，请返回[]）。
示例输入：{"text":"XiaoHong and XiaoMing are studying DB class.","keywords":[["DB","数据库"],["XiaoHong","萧红"]]}
示例输出：{"text":"萧红和晓明正在学习数据库课程","keywords":[["XiaoMing","晓明"]]}。
你的输出必须为JSON格式"""

class TestLongTextChatBot: ServerChatBot() {
    override var args: HashMap<String, Any?> = hashMapOf()

    override suspend fun sendRequest(formattedText: String, args: Map<String, Any?>): Flow<String> {
        return flow {
            emit("{\"text\":\"晓明告诉萧红，这个班级里最优秀的学生是晓张。\",\"keywords\":[[\"XiaoZhang\",\"晓张\"]]}")
        }
    }

    override fun getFormattedText(
        systemPrompt: String,
        includedMessages: List<ChatMessage>
    ): String {
        // {"text":"XiaoMing told XiaoHong, the best student in this class is XiaoZhang."}
        return buildString {
            append("System: ")
            append(systemPrompt)
            append("\n")
            val obj = JSONObject()
            obj.put("text", includedMessages.last().content)
            val keywords = args["keywords"] as? List<List<String>>
            if (keywords != null) {
                obj.put("keywords", JSONArray(keywords))
            }
            append("Input: ")
            append(obj.toString())
        }
    }

    override val id: Int = 0
    override val name: String = "Test"
    override val avatar: String = "https://c-ssl.duitang.com/uploads/blog/202206/12/20220612164733_72d8b.jpg"
    override val maxContextLength = 1024
}