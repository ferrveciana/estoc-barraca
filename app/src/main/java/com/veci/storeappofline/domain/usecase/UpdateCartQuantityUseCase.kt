package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.domain.model.CartItem
import com.veci.storeappofline.domain.repository.StoreRepository
import javax.inject.Inject

class UpdateCartQuantityUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(cartItem: CartItem, newQuantity: Int): Boolean {
        val product = repository.getProductById(cartItem.productId)
        
        if (newQuantity <= 0) {
            repository.removeFromCart(cartItem.id)
            if (product != null) {
                repository.updateProductStock(product.id, product.stockQuantity + cartItem.quantity)
            }
            return true
        }

        val diff = newQuantity - cartItem.quantity
        if (diff > 0) {
            if (product == null || product.stockQuantity < diff) {
                return false
            }
        }

        if (product != null) {
            repository.updateProductStock(product.id, product.stockQuantity - diff)
        }
        repository.updateCartItemQuantity(cartItem.id, newQuantity)
        return true
    }

    suspend fun remove(cartItem: CartItem) {
        invoke(cartItem, 0)
    }

    suspend fun clearCart(cartItems: List<CartItem>) {
        for (item in cartItems) {
            val product = repository.getProductById(item.productId)
            if (product != null) {
                repository.updateProductStock(product.id, product.stockQuantity + item.quantity)
            }
        }
        repository.clearCart()
    }
}
