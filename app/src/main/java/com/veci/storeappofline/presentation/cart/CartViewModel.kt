package com.veci.storeappofline.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.veci.storeappofline.domain.model.CartItem
import com.veci.storeappofline.domain.usecase.CalculateCartTotalUseCase
import com.veci.storeappofline.domain.usecase.CheckoutUseCase
import com.veci.storeappofline.domain.usecase.GetCartItemsUseCase
import com.veci.storeappofline.domain.usecase.UpdateCartQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val calculateCartTotalUseCase: CalculateCartTotalUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val checkoutUseCase: CheckoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState(isLoading = true))
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        observeCartData()
    }

    private fun observeCartData() {
        getCartItemsUseCase().onEach { cartItems ->
            val total = calculateCartTotalUseCase(cartItems)
            _uiState.update { current ->
                current.copy(
                    cartItems = cartItems,
                    totalPrice = total,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    fun increaseQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            val success = updateCartQuantityUseCase(cartItem, cartItem.quantity + 1)
            if (!success) {
                _uiState.update { it.copy(userMessage = "No hi ha més estoc disponible per ${cartItem.productName}!") }
            }
        }
    }

    fun decreaseQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            updateCartQuantityUseCase(cartItem, cartItem.quantity - 1)
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            updateCartQuantityUseCase.remove(cartItem)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            updateCartQuantityUseCase.clearCart(_uiState.value.cartItems)
        }
    }

    fun checkout() {
        viewModelScope.launch {
            val success = checkoutUseCase()
            if (success) {
                _uiState.update { it.copy(userMessage = "Comanda realitzada amb èxit! Gràcies per la seva compra.") }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
