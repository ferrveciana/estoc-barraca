package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.usecase.AddToCartUseCaseTest.FakeStoreRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckoutUseCaseTest {

    private lateinit var fakeRepository: FakeStoreRepository
    private lateinit var addToCartUseCase: AddToCartUseCase
    private lateinit var checkoutUseCase: CheckoutUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeStoreRepository()
        addToCartUseCase = AddToCartUseCase(fakeRepository)
        checkoutUseCase = CheckoutUseCase(fakeRepository)

        val product = Product(id = 1, name = "Coca-Cola", price = 2.5, stockQuantity = 10)
        fakeRepository.setProducts(listOf(product))
    }

    @Test
    fun `checkout empties cart and keeps product stock decremented`() = runTest {
        val product = fakeRepository.productsMap[1]!!
        addToCartUseCase(product)

        assertEquals(1, fakeRepository.cartItemsList.size)
        assertEquals(9, fakeRepository.productsMap[1]?.stockQuantity)

        val result = checkoutUseCase()

        assertTrue(result)
        assertEquals(0, fakeRepository.cartItemsList.size)
        // Stock remains consumed (9) after checkout
        assertEquals(9, fakeRepository.productsMap[1]?.stockQuantity)
    }
}
