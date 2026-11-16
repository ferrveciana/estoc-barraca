package com.veci.storeappofline.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val price: Double,
    val stockQuantity: Int,
    val category: ProductCategory = ProductCategory.BEVERAGE,
    val isSandwich: Boolean = false,
    val emoji: String = DEFAULT_EMOJI
) {
    companion object {
        const val DEFAULT_EMOJI = "🛒"
    }
}
