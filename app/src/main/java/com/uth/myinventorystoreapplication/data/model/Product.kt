package com.uth.myinventorystoreapplication.data.model

data class Product(
    val id: Long = 0,
    val name: String,
    val price: Double,
    val stock: Int,
    val barcode: String = "",
    val minStock: Int = 0
) {
    val totalValue: Double
        get() = price * stock

    val isLowStock: Boolean
        get() = stock <= minStock
}
