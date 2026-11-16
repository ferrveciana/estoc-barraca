package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.domain.model.CartItem
import javax.inject.Inject

class CalculateCartTotalUseCase @Inject constructor() {
    operator fun invoke(cartItems: List<CartItem>): Double {
        return cartItems.sumOf { it.totalPrice }
    }
}
