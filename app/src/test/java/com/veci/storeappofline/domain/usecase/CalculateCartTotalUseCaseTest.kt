package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.domain.model.CartItem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateCartTotalUseCaseTest {

    private lateinit var useCase: CalculateCartTotalUseCase

    @Before
    fun setUp() {
        useCase = CalculateCartTotalUseCase()
    }

    @Test
    fun `calculate total with empty list returns zero`() {
        val result = useCase(emptyList())
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `calculate total with single item returns correct subtotal`() {
        val cartItems = listOf(
            CartItem(id = 1, productId = 10, productName = "Headphones", unitPrice = 50.0, quantity = 2)
        )
        val result = useCase(cartItems)
        assertEquals(100.0, result, 0.001)
    }

    @Test
    fun `calculate total with multiple items returns correct total sum`() {
        val cartItems = listOf(
            CartItem(id = 1, productId = 10, productName = "Headphones", unitPrice = 50.0, quantity = 2), // 100.0
            CartItem(id = 2, productId = 11, productName = "Mouse", unitPrice = 25.5, quantity = 3),       // 76.5
            CartItem(id = 3, productId = 12, productName = "Keyboard", unitPrice = 100.0, quantity = 1)     // 100.0
        )
        val result = useCase(cartItems)
        assertEquals(276.5, result, 0.001)
    }
}
