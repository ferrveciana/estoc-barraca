package com.veci.storeappofline.data.stock

import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.model.ProductCategory
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object StockJsonCodec {

    fun export(products: List<Product>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", isoNow())
        val array = JSONArray()
        products.forEach { product ->
            array.put(
                JSONObject().apply {
                    put("id", product.id)
                    put("name", product.name)
                    put("price", product.price)
                    put("stockQuantity", product.stockQuantity)
                    put("category", product.category.name)
                    put("isSandwich", product.isSandwich)
                    put("emoji", product.emoji)
                }
            )
        }
        root.put("products", array)
        return root.toString(2)
    }

    fun parse(json: String): List<Product> {
        val trimmed = json.trim()
        require(trimmed.isNotEmpty()) { "El fitxer JSON està buit" }

        val root = JSONObject(trimmed)
        val productsArray = when {
            root.has("products") -> root.getJSONArray("products")
            else -> error("Format JSON no vàlid: falta l'array \"products\"")
        }

        val products = mutableListOf<Product>()
        for (i in 0 until productsArray.length()) {
            val item = productsArray.getJSONObject(i)
            val name = item.optString("name").trim()
            require(name.isNotEmpty()) { "Producte sense nom a la posició $i" }

            val category = try {
                ProductCategory.valueOf(item.optString("category", ProductCategory.FOOD.name))
            } catch (_: Exception) {
                ProductCategory.FOOD
            }

            products.add(
                Product(
                    id = item.optLong("id", 0L),
                    name = name,
                    price = item.optDouble("price", 0.0),
                    stockQuantity = item.optInt("stockQuantity", 0).coerceAtLeast(0),
                    category = category,
                    isSandwich = item.optBoolean("isSandwich", false) && category == ProductCategory.FOOD,
                    emoji = item.optString("emoji", Product.DEFAULT_EMOJI).ifBlank { Product.DEFAULT_EMOJI }
                )
            )
        }
        return products
    }

    private fun isoNow(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
    }
}
