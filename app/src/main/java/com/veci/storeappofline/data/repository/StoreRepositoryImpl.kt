package com.veci.storeappofline.data.repository

import android.content.Context
import com.veci.storeappofline.data.local.StoreDatabase
import com.veci.storeappofline.data.local.dao.CartItemDao
import com.veci.storeappofline.data.local.dao.ProductDao
import com.veci.storeappofline.data.local.entity.CartItemEntity
import com.veci.storeappofline.data.mapper.toDomain
import com.veci.storeappofline.data.mapper.toEntity
import com.veci.storeappofline.domain.model.CartItem
import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.model.ProductSupplements
import com.veci.storeappofline.domain.model.SupplementPrices
import com.veci.storeappofline.domain.repository.StoreRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class StoreRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val cartItemDao: CartItemDao,
    @param:ApplicationContext private val context: Context
) : StoreRepository {

    private val prefs = context.getSharedPreferences("store_prefs", Context.MODE_PRIVATE)

    private val _supplementPrices = MutableStateFlow(
        SupplementPrices(
            cheesePrice = prefs.getFloat("cheese_price", ProductSupplements.DEFAULT_CHEESE_PRICE.toFloat()).toDouble(),
            onionPrice = prefs.getFloat("onion_price", ProductSupplements.DEFAULT_ONION_PRICE.toFloat()).toDouble()
        )
    )

    override fun getProducts(): Flow<List<Product>> {
        return productDao.getProducts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartItemDao.getCartItems().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSupplementPrices(): Flow<SupplementPrices> = _supplementPrices.asStateFlow()

    override suspend fun getSupplementPricesOnce(): SupplementPrices = _supplementPrices.value

    override suspend fun updateCheesePrice(price: Double) {
        prefs.edit().putFloat("cheese_price", price.toFloat()).apply()
        _supplementPrices.update { it.copy(cheesePrice = price) }
    }

    override suspend fun updateOnionPrice(price: Double) {
        prefs.edit().putFloat("onion_price", price.toFloat()).apply()
        _supplementPrices.update { it.copy(onionPrice = price) }
    }

    override suspend fun getProductsOnce(): List<Product> {
        return productDao.getProductsOnce().map { it.toDomain() }
    }

    override suspend fun getProductById(productId: Long): Product? {
        return productDao.getProductById(productId)?.toDomain()
    }

    override suspend fun getProductByName(name: String): Product? {
        return productDao.getProductByName(name)?.toDomain()
    }

    override suspend fun getCartItemByProductId(
        productId: Long,
        withExtraCheese: Boolean,
        withExtraOnion: Boolean
    ): CartItem? {
        return cartItemDao.getCartItemByProductId(productId, withExtraCheese, withExtraOnion)?.toDomain()
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

        val cartItemEntity = CartItemEntity(
            productId = product.id,
            productName = productName,
            unitPrice = unitPrice,
            quantity = 1,
            withExtraCheese = withExtraCheese,
            withExtraOnion = withExtraOnion
        )
        cartItemDao.insertCartItem(cartItemEntity)
    }

    override suspend fun updateCartItemQuantity(cartItemId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            cartItemDao.deleteCartItemById(cartItemId)
        } else {
            cartItemDao.updateCartItemQuantity(cartItemId, newQuantity)
        }
    }

    override suspend fun removeFromCart(cartItemId: Long) {
        cartItemDao.deleteCartItemById(cartItemId)
    }

    override suspend fun clearCart() {
        cartItemDao.clearCart()
    }

    override suspend fun updateProductStock(productId: Long, newStock: Int) {
        productDao.updateProductStock(productId, newStock)
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }

    override suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product.toEntity().copy(id = 0))
    }

    override suspend fun resetSampleProducts() {
        productDao.insertProducts(StoreDatabase.DUMMY_PRODUCTS)
    }

    override suspend fun deleteProduct(productId: Long) {
        productDao.deleteProductById(productId)
    }
}
