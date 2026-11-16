package com.veci.storeappofline.data.stock

import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.model.ProductCategory
import com.veci.storeappofline.domain.usecase.AddToCartUseCaseTest.FakeStoreRepository
import com.veci.storeappofline.domain.usecase.ExportStockUseCase
import com.veci.storeappofline.domain.usecase.ImportStockUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockJsonCodecTest {

    @Test
    fun `export and parse round trip keeps product fields`() {
        val products = listOf(
            Product(
                id = 1,
                name = "Cervesa",
                price = 2.0,
                stockQuantity = 12,
                category = ProductCategory.BEVERAGE,
                emoji = "🍺"
            ),
            Product(
                id = 2,
                name = "Entrepà de Llom",
                price = 4.5,
                stockQuantity = 8,
                category = ProductCategory.FOOD,
                isSandwich = true,
                emoji = "🥩"
            )
        )

        val json = StockJsonCodec.export(products)
        val parsed = StockJsonCodec.parse(json)

        assertEquals(2, parsed.size)
        assertEquals("Cervesa", parsed[0].name)
        assertEquals(12, parsed[0].stockQuantity)
        assertEquals("🍺", parsed[0].emoji)
        assertTrue(parsed[1].isSandwich)
        assertEquals(8, parsed[1].stockQuantity)
    }

    @Test
    fun `import updates stock by id and creates missing products`() = runTest {
        val repo = FakeStoreRepository()
        repo.setProducts(
            listOf(
                Product(id = 1, name = "Cervesa", price = 2.0, stockQuantity = 5, emoji = "🍺")
            )
        )

        val json = """
            {
              "version": 1,
              "products": [
                {"id": 1, "name": "Cervesa", "price": 2.0, "stockQuantity": 40, "category": "BEVERAGE", "isSandwich": false, "emoji": "🍺"},
                {"id": 99, "name": "Roncola", "price": 5.0, "stockQuantity": 15, "category": "BEVERAGE", "isSandwich": false, "emoji": "🍹"}
              ]
            }
        """.trimIndent()

        val result = ImportStockUseCase(repo)(json)

        assertEquals(1, result.updated)
        assertEquals(1, result.created)
        assertEquals(40, repo.productsMap[1]?.stockQuantity)
        assertTrue(repo.productsMap.values.any { it.name == "Roncola" && it.stockQuantity == 15 })
    }

    @Test
    fun `export use case returns valid json`() = runTest {
        val repo = FakeStoreRepository()
        repo.setProducts(
            listOf(Product(id = 1, name = "Aigua", price = 1.5, stockQuantity = 20, emoji = "💧"))
        )
        val json = ExportStockUseCase(repo)()
        assertTrue(json.contains("Aigua"))
        assertTrue(json.contains("stockQuantity"))
    }
}
