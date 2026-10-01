package com.anime.app.model

data class Creator(
    val id: String,
    val displayName: String,
    val avatarUrl: String,
    val bio: String,
)

/** Admin publishes as this creator until the Creator studio exists. */
object PrototypeCreator {
    const val ID = "pampas"
}
