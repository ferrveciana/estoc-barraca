package com.veci.storeappofline.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.veci.storeappofline.presentation.cart.CartScreen
import com.veci.storeappofline.presentation.cart.CartViewModel
import com.veci.storeappofline.presentation.store.StoreScreen
import com.veci.storeappofline.presentation.store.StoreViewModel

object Routes {
    const val STORE = "store"
    const val CART = "cart"
}

@Composable
fun StoreAppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.STORE
    ) {
        composable(Routes.STORE) {
            val storeViewModel: StoreViewModel = hiltViewModel()
            StoreScreen(
                viewModel = storeViewModel,
                onNavigateToCart = { navController.navigate(Routes.CART) }
            )
        }

        composable(Routes.CART) {
            val cartViewModel: CartViewModel = hiltViewModel()
            CartScreen(
                viewModel = cartViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
