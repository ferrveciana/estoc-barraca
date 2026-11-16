package com.veci.storeappofline.presentation.store

import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.model.ProductCategory
import com.veci.storeappofline.domain.model.ProductSupplements

enum class StoreTab {
    BEVERAGES,
    FOOD,
    ALL,
    MANAGEMENT
}

data class StoreUiState(
    val products: List<Product> = emptyList(),
    val selectedTab: StoreTab = StoreTab.BEVERAGES,
    val totalCartItemsCount: Int = 0,
    val cheesePrice: Double = ProductSupplements.DEFAULT_CHEESE_PRICE,
    val onionPrice: Double = ProductSupplements.DEFAULT_ONION_PRICE,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val sandwichPendingSupplements: Product? = null
) {
    val sandwichPendingCheese: Product?
        get() = sandwichPendingSupplements

    val displayedProducts: List<Product>
        get() = when (selectedTab) {
            StoreTab.BEVERAGES -> products.filter { it.category == ProductCategory.BEVERAGE }
            StoreTab.FOOD -> products.filter { it.category == ProductCategory.FOOD }
            StoreTab.ALL -> products
            StoreTab.MANAGEMENT -> products
        }
}
