package com.veci.storeappofline.presentation.cart

import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.usecase.AddToCartUseCase
import com.veci.storeappofline.domain.usecase.AddToCartUseCaseTest.FakeStoreRepository
import com.veci.storeappofline.domain.usecase.CalculateCartTotalUseCase
import com.veci.storeappofline.domain.usecase.CheckoutUseCase
import com.veci.storeappofline.domain.usecase.GetCartItemsUseCase
import com.veci.storeappofline.domain.usecase.UpdateCartQuantityUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeStoreRepository
    private lateinit var viewModel: CartViewModel
    private lateinit var addToCartUseCase: AddToCartUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeStoreRepository()

        val product1 = Product(id = 1, name = "Coca-Cola", price = 2.50, stockQuantity = 5)
        fakeRepository.setProducts(listOf(product1))

        addToCartUseCase = AddToCartUseCase(fakeRepository)

        val getCartItemsUseCase = GetCartItemsUseCase(fakeRepository)
        val calculateCartTotalUseCase = CalculateCartTotalUseCase()
        val updateCartQuantityUseCase = UpdateCartQuantityUseCase(fakeRepository)
        val checkoutUseCase = CheckoutUseCase(fakeRepository)

        viewModel = CartViewModel(
            getCartItemsUseCase = getCartItemsUseCase,
            calculateCartTotalUseCase = calculateCartTotalUseCase,
            updateCartQuantityUseCase = updateCartQuantityUseCase,
            checkoutUseCase = checkoutUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cart viewmodel calculates total order price correctly`() = runTest {
        addToCartUseCase(fakeRepository.productsMap[1]!!)
        addToCartUseCase(fakeRepository.productsMap[1]!!)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.cartItems.size)
        assertEquals(2, state.cartItems.first().quantity)
        assertEquals(5.00, state.totalPrice, 0.001)
    }

    @Test
    fun `checkout empties cart items and sets confirmation message`() = runTest {
        addToCartUseCase(fakeRepository.productsMap[1]!!)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.checkout()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.cartItems.size)
        assertEquals(0.0, state.totalPrice, 0.001)
        assertEquals("Comanda realitzada amb èxit! Gràcies per la seva compra.", state.userMessage)
    }
}
