package com.vibecode.chatter.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class ChattsResponse(
    val chatts: List<List<String>>
)

data class PostChatRequest(
    val username: String,
    val message: String
)

interface ChatApi {
    @GET("getchatts/")
    suspend fun getMessages(): Response<ChattsResponse>

    @POST("postchatt/")
    suspend fun postMessage(@Body request: PostChatRequest): Response<Unit>
}
