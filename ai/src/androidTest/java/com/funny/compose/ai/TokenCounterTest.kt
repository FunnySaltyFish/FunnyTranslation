package com.funny.compose.ai

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.funny.compose.ai.chat.ChatMessageReq
import com.funny.compose.ai.token.OpenAITokenCounter
import com.funny.compose.ai.token.TokenCounter
import com.funny.translation.BaseApplication
import com.funny.translation.helper.JsonX
import com.funny.translation.network.NetworkReceiver
import com.funny.translation.network.OkHttpUtils
import com.hjq.toast.ToastUtils
import com.tencent.mmkv.MMKV
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.util.Properties

@SmallTest
class TokenCounterTest {
    private var openaiApiKey = ""

    class RemoteOpenAITokenCounter(val apiKey: String): TokenCounter() {
        override suspend fun encode(
            text: String,
            allowedSpecialTokens: Array<String>,
            maxTokenLength: Int
        ): List<Int> {
            return emptyList()
        }

        override suspend fun countMessages(
            systemPrompt: String,
            messages: List<ChatMessageReq>
        ): Int {
            // https://api.openai.com/v1/chat/completions   -H "Content-Type: application/json"   -H "Authorization: Bearer $OPENAI_API_KEY
            val messagesStr = JsonX.toJson(messages)
            println("messagesStr:\n$messagesStr")
            val resp = OkHttpUtils.postJSON(
                url = "https://api.openai-proxy.com/v1/chat/completions",
                json = """
                    { 
                        "model": "gpt-3.5-turbo",
                        "messages": ${messagesStr},
                        "max_tokens": 1
                    }
                """.trimIndent(),
                headers = hashMapOf(
                    "Authorization" to "Bearer $apiKey",
                )
            )
//            println("resp:\n$resp")
            val jsonObj = JSONObject(resp)
            val usage = jsonObj.getJSONObject("usage")
            val tokens = usage.getInt("prompt_tokens")
            return tokens
        }
    }

    @Before
    fun setup(): Unit {
        val resource = this.javaClass.classLoader?.getResource("keys.properties")
            ?: throw Exception("openaiApiKey is empty, please create keys.properties file in androidTest/resources and add OPENAI_API_KEY=sk-xxx to it")

        resource.openStream().use {
            val properties = Properties()
            properties.load(it)
            println("properties: $properties")
            openaiApiKey = properties.getProperty("OPENAI_API_KEY")
        }

        if (openaiApiKey.isEmpty()) {
            throw Exception("openaiApiKey is empty, please create keys.properties file in androidTest/resources and add OPENAI_API_KEY=sk-xxx to it")
        }

        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull(appContext)
        BaseApplication.ctx = appContext

        val application = ApplicationProvider.getApplicationContext<Application>()
        assertNotNull(application)
        ToastUtils.init(application)
        MMKV.initialize(application)
        NetworkReceiver.setNetworkState(appContext)
    }

    @Test
    fun testTokenCounter(): Unit {
        val systemPrompt = "You're ChatGPT, a helpful AI assistant"
        val messages = arrayOf(
            "Nice to meet you",
            "What can you do for me?",
            "中文也要来测试一下",
//            "java.lang.SecurityException: Permission denied (missing INTERNET permission?)",
//            "怎么办，{\n   \"type\": \"com.funny.compose.ai.chat.ChatMessageReq.Text\",\n   \"content\": \"Nice to meet you\",\n   \"role\": \"user\"\n}",
//            "行了"
        )
        runBlocking {
            val remoteCounter = RemoteOpenAITokenCounter(openaiApiKey)

            println("Start init local counter")
            val localCounter = OpenAITokenCounter()
            println("Finish init local counter")

            messages.forEach {
                count(localCounter, remoteCounter, systemPrompt, it)
            }
        }
    }

    private suspend fun count(counter1: TokenCounter, counter2: TokenCounter, systemPrompt: String, message: String) = coroutineScope {
        val messages = listOf( ChatMessageReq.text(message, "user") )
        val count1 = async {
            counter1.countMessages(systemPrompt, messages)
        }
        val count2 = async {
            counter2.countMessages(systemPrompt, messages )
        }
        println("-- msg: $messages")
        val num1 = count1.await()
        val num2 = count2.await()
        println("-- count1: $num1, count2: $num2")
        assert(num1 == num2)
    }
}