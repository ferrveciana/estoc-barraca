package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.repository.StoreRepository
import javax.inject.Inject

class InsertProductUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(product: Product): Long {
        require(product.name.isNotBlank()) { "El nom del producte no pot estar buit" }
        require(product.price >= 0) { "El preu no pot ser negatiu" }
        require(product.stockQuantity >= 0) { "L'estoc no pot ser negatiu" }
        return repository.insertProduct(product)
    }
}
