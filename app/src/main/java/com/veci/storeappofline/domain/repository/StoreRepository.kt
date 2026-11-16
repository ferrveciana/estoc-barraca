package com.veci.storeappofline.domain.repository

import com.veci.storeappofline.domain.model.CartItem
import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.model.ProductSupplements
import com.veci.storeappofline.domain.model.SupplementPrices
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun getProducts(): Flow<List<Product>>
    fun getCartItems(): Flow<List<CartItem>>
    fun getSupplementPrices(): Flow<SupplementPrices>
    suspend fun getSupplementPricesOnce(): SupplementPrices
    suspend fun updateCheesePrice(price: Double)
    suspend fun updateOnionPrice(price: Double)
    suspend fun getProductsOnce(): List<Product>
    suspend fun getProductById(productId: Long): Product?
    suspend fun getProductByName(name: String): Product?
    suspend fun getCartItemByProductId(
        productId: Long,
        withExtraCheese: Boolean = false,
        withExtraOnion: Boolean = false
    ): CartItem?
    suspend fun addToCart(
        product: Product,
        withExtraCheese: Boolean = false,
        withExtraOnion: Boolean = false,
        cheesePrice: Double = ProductSupplements.DEFAULT_CHEESE_PRICE,
        onionPrice: Double = ProductSupplements.DEFAULT_ONION_PRICE
    )
    suspend fun updateCartItemQuantity(cartItemId: Long, newQuantity: Int)
    suspend fun removeFromCart(cartItemId: Long)
    suspend fun clearCart()
    suspend fun updateProductStock(productId: Long, newStock: Int)
    suspend fun updateProduct(product: Product)
    suspend fun insertProduct(product: Product): Long
    suspend fun resetSampleProducts()
    suspend fun deleteProduct(productId: Long)
}
