package com.example.ryptomessage.data

data class Contact(
    val id: String,
    val nickname: String,
    val publicKey: String,
    val dateAdded: Long = System.currentTimeMillis()
)
