package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    operator fun invoke(): Flow<List<Product>> {
        return repository.getProducts()
    }
}
