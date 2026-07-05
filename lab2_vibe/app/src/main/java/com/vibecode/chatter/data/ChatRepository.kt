package com.vibecode.chatter.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ChatRepository {
    private val api: ChatApi

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ChatApi::class.java)
    }

    suspend fun getMessages(): Result<List<ChatMessage>> {
        return try {
            val response = api.getMessages()
            if (response.isSuccessful) {
                val chatts = response.body()?.chatts.orEmpty()
                val messages = chatts.mapNotNull { row ->
                    if (row.size >= 3) {
                        ChatMessage(
                            username = row[0],
                            message = row[1],
                            timestamp = row[2]
                        )
                    } else {
                        null
                    }
                }
                Result.success(messages)
            } else {
                Result.failure(Exception("Failed to load messages (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun postMessage(username: String, message: String): Result<Unit> {
        return try {
            val response = api.postMessage(PostChatRequest(username, message))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to post message (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val BASE_URL = "https://121.40.250.82/"
    }
}
