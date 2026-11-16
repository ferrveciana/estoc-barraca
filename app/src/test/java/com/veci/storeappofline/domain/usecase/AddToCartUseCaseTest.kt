package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.domain.model.CartItem
import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.model.ProductSupplements
import com.veci.storeappofline.domain.model.SupplementPrices
import com.veci.storeappofline.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddToCartUseCaseTest {

    private lateinit var fakeRepository: FakeStoreRepository
    private lateinit var useCase: AddToCartUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeStoreRepository()
        useCase = AddToCartUseCase(fakeRepository)
    }

    @Test
    fun `add to cart with zero stock returns false`() = runTest {
        val product = Product(id = 1, name = "Out of stock item", price = 10.0, stockQuantity = 0)

        val result = useCase(product)

        assertFalse(result)
        assertEquals(0, fakeRepository.cartItemsList.size)
    }

    @Test
    fun `add to cart with available stock adds item and decrements stock`() = runTest {
        val product = Product(id = 1, name = "Available item", price = 10.0, stockQuantity = 5)
        fakeRepository.setProducts(listOf(product))

        val result = useCase(product)

        assertTrue(result)
        assertEquals(1, fakeRepository.cartItemsList.size)
        assertEquals(4, fakeRepository.productsMap[1]?.stockQuantity)
    }

    @Test
    fun `add sandwich with cheese increases price by cheese price`() = runTest {
        val product = Product(
            id = 2,
            name = "Entrepà de Llom",
            price = 4.50,
            stockQuantity = 5,
            isSandwich = true
        )
        fakeRepository.setProducts(listOf(product))

        val result = useCase(product, withExtraCheese = true)

        assertTrue(result)
        assertEquals(1, fakeRepository.cartItemsList.size)
        val cartItem = fakeRepository.cartItemsList.first()
        assertTrue(cartItem.withExtraCheese)
        assertFalse(cartItem.withExtraOnion)
        assertEquals(4.50 + ProductSupplements.DEFAULT_CHEESE_PRICE, cartItem.unitPrice, 0.001)
        assertEquals("Entrepà de Llom + Formatge", cartItem.productName)
    }

    @Test
    fun `add sandwich with onion increases price by onion price`() = runTest {
        val product = Product(
            id = 2,
            name = "Entrepà de Bacon",
            price = 4.50,
            stockQuantity = 5,
            isSandwich = true
        )
        fakeRepository.setProducts(listOf(product))

        val result = useCase(product, withExtraOnion = true)

        assertTrue(result)
        assertEquals(1, fakeRepository.cartItemsList.size)
        val cartItem = fakeRepository.cartItemsList.first()
        assertFalse(cartItem.withExtraCheese)
        assertTrue(cartItem.withExtraOnion)
        assertEquals(4.50 + ProductSupplements.DEFAULT_ONION_PRICE, cartItem.unitPrice, 0.001)
        assertEquals("Entrepà de Bacon + Ceba", cartItem.productName)
    }

    @Test
    fun `add sandwich with both cheese and onion increases price accordingly`() = runTest {
        val product = Product(
            id = 2,
            name = "Entrepà de Botifarra",
            price = 4.50,
            stockQuantity = 5,
            isSandwich = true
        )
        fakeRepository.setProducts(listOf(product))
        fakeRepository.updateCheesePrice(1.20)
        fakeRepository.updateOnionPrice(0.80)

        val result = useCase(product, withExtraCheese = true, withExtraOnion = true)

        assertTrue(result)
        assertEquals(1, fakeRepository.cartItemsList.size)
        val cartItem = fakeRepository.cartItemsList.first()
        assertTrue(cartItem.withExtraCheese)
        assertTrue(cartItem.withExtraOnion)
        assertEquals(4.50 + 1.20 + 0.80, cartItem.unitPrice, 0.001)
        assertEquals("Entrepà de Botifarra + Formatge + Ceba", cartItem.productName)
    }

    class FakeStoreRepository : StoreRepository {
        val productsMap = mutableMapOf<Long, Product>()
        val cartItemsList = mutableListOf<CartItem>()

        private val _productsFlow = MutableStateFlow<List<Product>>(emptyList())
        private val _cartItemsFlow = MutableStateFlow<List<CartItem>>(emptyList())
        private val _supplementPricesFlow = MutableStateFlow(SupplementPrices())

        private var cartIdCounter = 1L
        private var productIdCounter = 100L

        fun setProducts(list: List<Product>) {
            productsMap.clear()
            list.forEach { productsMap[it.id] = it }
            notifyProductsChanged()
        }

        private fun notifyProductsChanged() {
            _productsFlow.value = productsMap.values.toList()
        }

        private fun notifyCartChanged() {
            _cartItemsFlow.value = cartItemsList.toList()
        }

        override fun getProducts(): Flow<List<Product>> = _productsFlow.asStateFlow()

        override fun getCartItems(): Flow<List<CartItem>> = _cartItemsFlow.asStateFlow()

        override fun getSupplementPrices(): Flow<SupplementPrices> = _supplementPricesFlow.asStateFlow()

        override suspend fun getSupplementPricesOnce(): SupplementPrices = _supplementPricesFlow.value

        override suspend fun updateCheesePrice(price: Double) {
            _supplementPricesFlow.value = _supplementPricesFlow.value.copy(cheesePrice = price)
        }

        override suspend fun updateOnionPrice(price: Double) {
            _supplementPricesFlow.value = _supplementPricesFlow.value.copy(onionPrice = price)
        }

        override suspend fun getProductById(productId: Long): Product? = productsMap[productId]

        override suspend fun getProductsOnce(): List<Product> = productsMap.values.toList()

        override suspend fun getProductByName(name: String): Product? {
            return productsMap.values.find { it.name.equals(name, ignoreCase = false) }
        }

        override suspend fun getCartItemByProductId(
            productId: Long,
            withExtraCheese: Boolean,
            withExtraOnion: Boolean
        ): CartItem? {
            return cartItemsList.find {
                it.productId == productId &&
                    it.withExtraCheese == withExtraCheese &&
                    it.withExtraOnion == withExtraOnion
            }
        }

        override suspend fun addToCart(
            product: Product,
            withExtraCheese: Boolean,
            withExtraOnion: Boolean,
            cheesePrice: Double,
            onionPrice: Double
        ) {
            var unitPrice = product.price
            val extras = mutableListOf<String>()
            if (withExtraCheese) {
                unitPrice += cheesePrice
                extras.add(ProductSupplements.CHEESE_LABEL)
            }
            if (withExtraOnion) {
                unitPrice += onionPrice
                extras.add(ProductSupplements.ONION_LABEL)
            }

            val productName = if (extras.isNotEmpty()) {
                "${product.name} + ${extras.joinToString(" + ")}"
            } else {
                product.name
            }

            cartItemsList.add(
                CartItem(
                    id = cartIdCounter++,
                    productId = product.id,
                    productName = productName,
                    unitPrice = unitPrice,
                    quantity = 1,
                    withExtraCheese = withExtraCheese,
                    withExtraOnion = withExtraOnion
                )
            )
            notifyCartChanged()
        }

        override suspend fun updateCartItemQuantity(cartItemId: Long, newQuantity: Int) {
            val index = cartItemsList.indexOfFirst { it.id == cartItemId }
            if (index != -1) {
                if (newQuantity <= 0) {
                    cartItemsList.removeAt(index)
                } else {
                    cartItemsList[index] = cartItemsList[index].copy(quantity = newQuantity)
                }
                notifyCartChanged()
            }
        }

        override suspend fun removeFromCart(cartItemId: Long) {
            if (cartItemsList.removeAll { it.id == cartItemId }) {
                notifyCartChanged()
            }
        }

        override suspend fun clearCart() {
            cartItemsList.clear()
            notifyCartChanged()
        }

        override suspend fun updateProductStock(productId: Long, newStock: Int) {
            productsMap[productId]?.let {
                productsMap[productId] = it.copy(stockQuantity = newStock)
                notifyProductsChanged()
            }
        }

        override suspend fun updateProduct(product: Product) {
            productsMap[product.id] = product
            notifyProductsChanged()
        }

        override suspend fun insertProduct(product: Product): Long {
            val id = if (product.id == 0L) productIdCounter++ else product.id
            productsMap[id] = product.copy(id = id)
            notifyProductsChanged()
            return id
        }

        override suspend fun resetSampleProducts() {
            setProducts(
                listOf(
                    Product(id = 1, name = "Cervesa", price = 2.0, stockQuantity = 60),
                    Product(id = 2, name = "Entrepà de Llom", price = 4.5, stockQuantity = 40, isSandwich = true)
                )
            )
        }

        override suspend fun deleteProduct(productId: Long) {
            productsMap.remove(productId)
            notifyProductsChanged()
        }
    }
}
