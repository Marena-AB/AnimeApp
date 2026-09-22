package com.anime.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.decodeToImageBitmap

data class Post(val id: Int, val description: String, val image: ImageBitmap?)

@Composable
fun App() {
    MaterialTheme {
        val posts = remember { mutableStateListOf<Post>() }
        var draft by remember { mutableStateOf("") }
        var pickedImage by remember { mutableStateOf<ImageBitmap?>(null) }
        var nextId by remember { mutableIntStateOf(0) }
        val scope = rememberCoroutineScope()

        val picker = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
            if (file != null) {
                scope.launch {
                    pickedImage = file.readBytes().decodeToImageBitmap()
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize().safeContentPadding()) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { picker.launch() }) { Text("Choose photo") }

            pickedImage?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Button(
                enabled = draft.isNotBlank(),
                onClick = {
                    posts.add(0, Post(nextId++, draft.trim(), pickedImage))
                    draft = ""
                    pickedImage = null
                }
            ) { Text("Post") }

            LazyColumn {
                items(posts, key = { it.id }) { post ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(post.description)
                        post.image?.let {
                            Image(
                                bitmap = it,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}