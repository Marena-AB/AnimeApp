package com.anime.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    MaterialTheme {
        val posts = remember { mutableStateListOf<String>() }
        var draft by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize().safeContentPadding()) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (draft.isNotBlank()) {
                        posts.add(0, draft.trim())
                        draft = ""
                    }
                }
            ) { Text("Post") }

            LazyColumn {
                items(posts) { post ->
                    Text(post, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}