package com.veci.storeappofline.domain.model

data class SupplementPrices(
    val cheesePrice: Double = ProductSupplements.DEFAULT_CHEESE_PRICE,
    val onionPrice: Double = ProductSupplements.DEFAULT_ONION_PRICE
)

object ProductSupplements {
    const val DEFAULT_CHEESE_PRICE = 1.0
    const val DEFAULT_ONION_PRICE = 0.5
    const val CHEESE_PRICE = DEFAULT_CHEESE_PRICE // For backward compatibility if needed
    const val CHEESE_LABEL = "Formatge"
    const val ONION_LABEL = "Ceba"
}
