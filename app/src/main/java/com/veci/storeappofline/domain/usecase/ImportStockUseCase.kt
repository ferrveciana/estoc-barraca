package com.veci.storeappofline.domain.usecase

import com.veci.storeappofline.data.stock.StockJsonCodec
import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.model.ProductCategory
import com.veci.storeappofline.domain.model.StockImportResult
import com.veci.storeappofline.domain.repository.StoreRepository
import javax.inject.Inject

class ImportStockUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(json: String): StockImportResult {
        val imported = StockJsonCodec.parse(json)
        var updated = 0
        var created = 0

        for (item in imported) {
            val existing = findExisting(item)
            if (existing != null) {
                repository.updateProduct(
                    existing.copy(
                        name = item.name,
                        price = item.price,
                        stockQuantity = item.stockQuantity,
                        category = item.category,
                        isSandwich = item.isSandwich && item.category == ProductCategory.FOOD,
                        emoji = item.emoji.ifBlank { existing.emoji }
                    )
                )
                updated++
            } else {
                repository.insertProduct(
                    item.copy(
                        id = 0,
                        isSandwich = item.isSandwich && item.category == ProductCategory.FOOD
                    )
                )
                created++
            }
        }

        return StockImportResult(updated = updated, created = created)
    }

    private suspend fun findExisting(item: Product): Product? {
        if (item.id > 0) {
            repository.getProductById(item.id)?.let { return it }
        }
        return repository.getProductByName(item.name)
    }
}
