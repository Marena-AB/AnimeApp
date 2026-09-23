package com.anime.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

@Composable
fun UploadTab(onPost: (title: String, description: String, image: ImageBitmap?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pickedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()

    val picker = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        if (file != null) {
            scope.launch {
                pickedImage = file.readBytes().decodeToImageBitmap()
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
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
            enabled = title.isNotBlank() && description.isNotBlank(),
            onClick = {
                onPost(title.trim(), description.trim(), pickedImage)
                description = ""
                pickedImage = null
            }
        ) { Text("Post") }
    }
}