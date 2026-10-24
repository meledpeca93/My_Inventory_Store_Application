package com.uth.myinventorystoreapplication.data.model

data class Movement(
    val id: Long = 0,
    val productId: Long,
    val qty: Int,
    val type: String, // "ENTRADA" o "SALIDA"
    val date: String,
    val productName: String = ""
)
