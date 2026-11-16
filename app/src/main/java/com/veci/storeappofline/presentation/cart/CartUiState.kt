package com.veci.storeappofline.presentation.cart

import com.veci.storeappofline.domain.model.CartItem

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)
