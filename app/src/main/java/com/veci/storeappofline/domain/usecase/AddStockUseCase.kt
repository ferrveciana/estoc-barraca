package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.domain.repository.StoreRepository
import javax.inject.Inject

class AddStockUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(productId: Long, amount: Int): Boolean {
        if (amount <= 0) return false
        val product = repository.getProductById(productId) ?: return false
        repository.updateProductStock(productId, product.stockQuantity + amount)
        return true
    }
}
