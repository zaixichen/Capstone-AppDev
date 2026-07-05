package com.vibecode.chatter.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    onNavigateBack: () -> Unit,
    onPostSuccess: () -> Unit = {},
    viewModel: PostViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedUsername by viewModel.savedUsername.collectAsState()
    val event by viewModel.events.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(savedUsername) {
        if (uiState.username.isEmpty() && savedUsername.isNotEmpty()) {
            viewModel.updateUsername(savedUsername)
        }
    }

    LaunchedEffect(event) {
        when (val currentEvent = event) {
            PostEvent.Success -> {
                viewModel.consumeEvent()
                onPostSuccess()
                onNavigateBack()
            }

            is PostEvent.Failure -> {
                Toast.makeText(context, currentEvent.message, Toast.LENGTH_SHORT).show()
                viewModel.consumeEvent()
            }

            null -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::updateUsername,
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.message,
                onValueChange = viewModel::updateMessage,
                label = { Text("Message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            Button(
                onClick = viewModel::sendMessage,
                enabled = !uiState.isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                if (uiState.isSending) {
                    CircularProgressIndicator()
                } else {
                    Text("Send")
                }
            }
        }
    }
}
