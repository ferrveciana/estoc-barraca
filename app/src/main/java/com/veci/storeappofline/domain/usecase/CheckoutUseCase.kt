package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.domain.repository.StoreRepository
import javax.inject.Inject

class CheckoutUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(): Boolean {
        repository.clearCart()
        return true
    }
}
