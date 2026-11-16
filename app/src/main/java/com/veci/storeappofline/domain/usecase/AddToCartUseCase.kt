package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.repository.StoreRepository
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(
        product: Product,
        withExtraCheese: Boolean = false,
        withExtraOnion: Boolean = false
    ): Boolean {
        if (product.stockQuantity <= 0) {
            return false
        }

        val extraCheese = withExtraCheese && product.isSandwich
        val extraOnion = withExtraOnion && product.isSandwich
        val existingCartItem = repository.getCartItemByProductId(product.id, extraCheese, extraOnion)

        if (existingCartItem != null) {
            repository.updateCartItemQuantity(existingCartItem.id, existingCartItem.quantity + 1)
        } else {
            val supplementPrices = repository.getSupplementPricesOnce()
            repository.addToCart(
                product = product,
                withExtraCheese = extraCheese,
                withExtraOnion = extraOnion,
                cheesePrice = supplementPrices.cheesePrice,
                onionPrice = supplementPrices.onionPrice
            )
        }

        repository.updateProductStock(product.id, product.stockQuantity - 1)
        return true
    }
}
