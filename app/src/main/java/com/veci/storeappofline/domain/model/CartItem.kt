package com.veci.storeappofline.domain.model

data class CartItem(
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val withExtraCheese: Boolean = false,
    val withExtraOnion: Boolean = false
) {
    val totalPrice: Double
        get() = unitPrice * quantity
}
