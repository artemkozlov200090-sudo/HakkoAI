package com.example.hakkoai

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(val role: String, val text: String)

object ChatApi {
    fun reply(character: String, messages: List<ChatMessage>): String {
        val endpoint = BuildConfig.CHAT_API_URL
        require(endpoint.startsWith("https://")) {
            "Сервер ИИ ещё не настроен. Нужен HTTPS-адрес backend."
        }

        val history = JSONArray()
        messages.takeLast(16).forEach { message ->
            history.put(JSONObject().put("role", message.role).put("content", message.text))
        }

        val payload = JSONObject()
            .put("character", character)
            .put("messages", history)

        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }

        try {
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val json = JSONObject(responseText)
            if (status !in 200..299) {
                throw IllegalStateException(json.optString("error", "Ошибка сервера ($status)"))
            }
            return json.optString("reply").takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("ИИ вернул пустой ответ.")
        } finally {
            connection.disconnect()
        }
    }
}
