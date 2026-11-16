package com.veci.storeappofline.presentation.store

import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.model.ProductCategory
import com.veci.storeappofline.domain.usecase.AddStockUseCase
import com.veci.storeappofline.domain.usecase.AddToCartUseCase
import com.veci.storeappofline.domain.usecase.AddToCartUseCaseTest.FakeStoreRepository
import com.veci.storeappofline.domain.usecase.ExportStockUseCase
import com.veci.storeappofline.domain.usecase.GetCartItemsUseCase
import com.veci.storeappofline.domain.usecase.GetProductsUseCase
import com.veci.storeappofline.domain.usecase.ImportStockUseCase
import com.veci.storeappofline.domain.usecase.InsertProductUseCase
import com.veci.storeappofline.domain.usecase.RemoveStockUseCase
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
class StoreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeStoreRepository
    private lateinit var viewModel: StoreViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeStoreRepository()

        val product1 = Product(id = 1, name = "Coca-Cola", price = 2.5, stockQuantity = 10, category = ProductCategory.BEVERAGE, emoji = "🥤")
        val product2 = Product(id = 2, name = "Entrepà de Pernil", price = 4.5, stockQuantity = 5, category = ProductCategory.FOOD, isSandwich = true, emoji = "🍖")
        fakeRepository.setProducts(listOf(product1, product2))

        viewModel = StoreViewModel(
            getProductsUseCase = GetProductsUseCase(fakeRepository),
            getCartItemsUseCase = GetCartItemsUseCase(fakeRepository),
            addToCartUseCase = AddToCartUseCase(fakeRepository),
            insertProductUseCase = InsertProductUseCase(fakeRepository),
            addStockUseCase = AddStockUseCase(fakeRepository),
            removeStockUseCase = RemoveStockUseCase(fakeRepository),
            exportStockUseCase = ExportStockUseCase(fakeRepository),
            importStockUseCase = ImportStockUseCase(fakeRepository),
            storeRepository = fakeRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `store viewmodel filters products by category`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBeverages = viewModel.uiState.value
        assertFalse(stateBeverages.isLoading)
        assertEquals(1, stateBeverages.displayedProducts.size)
        assertEquals("Coca-Cola", stateBeverages.displayedProducts.first().name)

        viewModel.selectTab(StoreTab.FOOD)
        val stateFood = viewModel.uiState.value
        assertEquals(1, stateFood.displayedProducts.size)
        assertEquals("Entrepà de Pernil", stateFood.displayedProducts.first().name)

        viewModel.selectTab(StoreTab.ALL)
        val stateAll = viewModel.uiState.value
        assertEquals(2, stateAll.displayedProducts.size)
    }

    @Test
    fun `add to cart out of stock sets Catalan user message`() = runTest {
        val outOfStockProduct = Product(id = 3, name = "Sense estoc", price = 1.0, stockQuantity = 0)
        viewModel.addToCart(outOfStockProduct)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("No hi ha prou estoc disponible!", viewModel.uiState.value.userMessage)

        viewModel.userMessageShown()
        assertEquals(null, viewModel.uiState.value.userMessage)
    }

    @Test
    fun `update supplement prices updates state`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateCheesePrice(1.50)
        viewModel.updateOnionPrice(0.75)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1.50, viewModel.uiState.value.cheesePrice, 0.001)
        assertEquals(0.75, viewModel.uiState.value.onionPrice, 0.001)
    }

    @Test
    fun `remove stock decrements product stock`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.removeStock(1, 3)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(7, fakeRepository.productsMap[1]?.stockQuantity ?: 0)
    }

    @Test
    fun `clear stock zeroes product stock`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearStock(1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, fakeRepository.productsMap[1]?.stockQuantity ?: -1)
    }

    @Test
    fun `delete product removes product from repository`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteProduct(1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.products.size)
        assertEquals(2L, viewModel.uiState.value.products.first().id)
    }
}
