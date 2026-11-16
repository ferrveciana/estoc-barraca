package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.domain.model.CartItem
import com.veci.storeappofline.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartItemsUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    operator fun invoke(): Flow<List<CartItem>> {
        return repository.getCartItems()
    }
}
