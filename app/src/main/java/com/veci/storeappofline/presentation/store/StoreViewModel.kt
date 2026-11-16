package com.veci.storeappofline.presentation.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.model.ProductCategory
import com.veci.storeappofline.domain.repository.StoreRepository
import com.veci.storeappofline.domain.usecase.AddStockUseCase
import com.veci.storeappofline.domain.usecase.AddToCartUseCase
import com.veci.storeappofline.domain.usecase.ExportStockUseCase
import com.veci.storeappofline.domain.usecase.GetCartItemsUseCase
import com.veci.storeappofline.domain.usecase.GetProductsUseCase
import com.veci.storeappofline.domain.usecase.ImportStockUseCase
import com.veci.storeappofline.domain.usecase.InsertProductUseCase
import com.veci.storeappofline.domain.usecase.RemoveStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val insertProductUseCase: InsertProductUseCase,
    private val addStockUseCase: AddStockUseCase,
    private val removeStockUseCase: RemoveStockUseCase,
    private val exportStockUseCase: ExportStockUseCase,
    private val importStockUseCase: ImportStockUseCase,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState(isLoading = true))
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    init {
        observeStoreData()
    }

    private fun observeStoreData() {
        combine(
            getProductsUseCase(),
            getCartItemsUseCase(),
            storeRepository.getSupplementPrices()
        ) { products, cartItems, supplementPrices ->
            Triple(products, cartItems.sumOf { it.quantity }, supplementPrices)
        }.onEach { (products, totalCartCount, supplementPrices) ->
            _uiState.update { current ->
                current.copy(
                    products = products,
                    totalCartItemsCount = totalCartCount,
                    cheesePrice = supplementPrices.cheesePrice,
                    onionPrice = supplementPrices.onionPrice,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    fun selectTab(tab: StoreTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onAddProductClicked(product: Product) {
        if (product.isSandwich) {
            _uiState.update { it.copy(sandwichPendingSupplements = product) }
        } else {
            addToCart(product, withExtraCheese = false, withExtraOnion = false)
        }
    }

    fun dismissCheeseDialog() {
        _uiState.update { it.copy(sandwichPendingSupplements = null) }
    }

    fun confirmSandwichAdd(withExtraCheese: Boolean = false, withExtraOnion: Boolean = false) {
        val product = _uiState.value.sandwichPendingSupplements ?: return
        _uiState.update { it.copy(sandwichPendingSupplements = null) }
        addToCart(product, withExtraCheese = withExtraCheese, withExtraOnion = withExtraOnion)
    }

    fun addToCart(
        product: Product,
        withExtraCheese: Boolean = false,
        withExtraOnion: Boolean = false
    ) {
        viewModelScope.launch {
            val success = addToCartUseCase(
                product = product,
                withExtraCheese = withExtraCheese,
                withExtraOnion = withExtraOnion
            )
            if (!success) {
                _uiState.update { it.copy(userMessage = "No hi ha prou estoc disponible!") }
            }
        }
    }

    fun updateCheesePrice(price: Double) {
        viewModelScope.launch {
            storeRepository.updateCheesePrice(price)
            _uiState.update { it.copy(userMessage = "Preu del formatge actualitzat") }
        }
    }

    fun updateOnionPrice(price: Double) {
        viewModelScope.launch {
            storeRepository.updateOnionPrice(price)
            _uiState.update { it.copy(userMessage = "Preu de la ceba actualitzat") }
        }
    }

    fun resetSampleProducts() {
        viewModelScope.launch {
            try {
                storeRepository.resetSampleProducts()
                _uiState.update { it.copy(userMessage = "Productes de mostra carregats correctament") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Error en carregar els productes") }
            }
        }
    }

    fun addStock(productId: Long, amount: Int) {
        viewModelScope.launch {
            val success = addStockUseCase(productId, amount)
            _uiState.update {
                it.copy(
                    userMessage = if (success) {
                        "Estoc actualitzat correctament"
                    } else {
                        "No s'ha pogut afegir estoc"
                    }
                )
            }
        }
    }

    fun removeStock(productId: Long, amount: Int) {
        viewModelScope.launch {
            val success = removeStockUseCase(productId, amount)
            _uiState.update {
                it.copy(
                    userMessage = if (success) {
                        "Estoc reduït correctament"
                    } else {
                        "No s'ha pogut reduir l'estoc"
                    }
                )
            }
        }
    }

    fun clearStock(productId: Long) {
        viewModelScope.launch {
            storeRepository.updateProductStock(productId, 0)
            _uiState.update { it.copy(userMessage = "Estoc posat a 0") }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            try {
                storeRepository.deleteProduct(productId)
                _uiState.update { it.copy(userMessage = "Producte eliminat correctament") }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message ?: "Error en eliminar el producte") }
            }
        }
    }

    fun addNewProduct(
        name: String,
        price: Double,
        stock: Int,
        category: ProductCategory,
        isSandwich: Boolean,
        emoji: String
    ) {
        viewModelScope.launch {
            try {
                insertProductUseCase(
                    Product(
                        name = name.trim(),
                        price = price,
                        stockQuantity = stock,
                        category = category,
                        isSandwich = isSandwich && category == ProductCategory.FOOD,
                        emoji = emoji.ifBlank { Product.DEFAULT_EMOJI }
                    )
                )
                _uiState.update { it.copy(userMessage = "Producte afegit correctament") }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userMessage = e.message ?: "Error en afegir el producte")
                }
            }
        }
    }

    fun prepareExportJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = exportStockUseCase()
                onReady(json)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(userMessage = e.message ?: "Error en exportar l'estoc")
                }
            }
        }
    }

    fun importStockJson(json: String) {
        viewModelScope.launch {
            try {
                val result = importStockUseCase(json)
                _uiState.update {
                    it.copy(
                        userMessage = "Importació: ${result.updated} actualitzats, ${result.created} creats"
                    )
                }
            } catch (e: Exception) {
                onImportError(e.message)
            }
        }
    }

    fun onExportSuccess() {
        _uiState.update { it.copy(userMessage = "Estoc exportat correctament") }
    }

    fun onExportError(message: String?) {
        _uiState.update { it.copy(userMessage = message ?: "Error en exportar l'estoc") }
    }

    fun onImportError(message: String?) {
        _uiState.update { it.copy(userMessage = message ?: "Error en importar l'estoc") }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
