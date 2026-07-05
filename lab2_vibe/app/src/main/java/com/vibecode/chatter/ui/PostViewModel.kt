package com.vibecode.chatter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vibecode.chatter.data.ChatRepository
import com.vibecode.chatter.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PostUiState(
    val username: String = "",
    val message: String = "",
    val isSending: Boolean = false
)

sealed class PostEvent {
    data object Success : PostEvent()
    data class Failure(val message: String) : PostEvent()
}

class PostViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ChatRepository()

    private val userPreferences = UserPreferences(application)

    val savedUsername: StateFlow<String> = userPreferences.lastUsername
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<PostEvent?>(null)
    val events: StateFlow<PostEvent?> = _events.asStateFlow()

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun updateMessage(value: String) {
        _uiState.update { it.copy(message = value) }
    }

    fun sendMessage() {
        val username = _uiState.value.username.trim()
        val message = _uiState.value.message.trim()

        if (username.isEmpty() || message.isEmpty()) {
            _events.value = PostEvent.Failure("Username and message are required")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            repository.postMessage(username, message)
                .onSuccess {
                    userPreferences.saveUsername(username)
                    _uiState.update { it.copy(isSending = false, message = "") }
                    _events.value = PostEvent.Success
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSending = false) }
                    _events.value = PostEvent.Failure(
                        error.message ?: "Failed to send message"
                    )
                }
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}
