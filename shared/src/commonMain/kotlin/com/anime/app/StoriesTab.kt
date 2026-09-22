package com.anime.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun StoriesTab(posts: List<Post>) {
    LazyColumn {
        items(posts, key = { it.id }) { post ->
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(post.title, style = MaterialTheme.typography.titleMedium)
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