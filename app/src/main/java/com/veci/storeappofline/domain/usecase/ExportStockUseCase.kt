package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.data.stock.StockJsonCodec
import com.veci.storeappofline.domain.repository.StoreRepository
import javax.inject.Inject

class ExportStockUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(): String {
        val products = repository.getProductsOnce()
        return StockJsonCodec.export(products)
    }
}
