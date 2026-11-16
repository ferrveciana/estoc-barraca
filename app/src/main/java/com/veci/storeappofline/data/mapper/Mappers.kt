package com.veci.storeappofline.data.mapper

import com.veci.storeappofline.data.local.entity.CartItemEntity
import com.veci.storeappofline.data.local.entity.ProductEntity
import com.veci.storeappofline.domain.model.CartItem
import com.veci.storeappofline.domain.model.Product
import com.veci.storeappofline.domain.model.ProductCategory

fun ProductEntity.toDomain(): Product {
    val domainCategory = try {
        ProductCategory.valueOf(category)
    } catch (e: Exception) {
        ProductCategory.BEVERAGE
    }
    return Product(
        id = id,
        name = name,
        price = price,
        stockQuantity = stockQuantity,
        category = domainCategory,
        isSandwich = isSandwich,
        emoji = emoji.ifBlank { Product.DEFAULT_EMOJI }
    )
}

fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        name = name,
        price = price,
        stockQuantity = stockQuantity,
        category = category.name,
        isSandwich = isSandwich,
        emoji = emoji.ifBlank { Product.DEFAULT_EMOJI }
    )
}

fun CartItemEntity.toDomain(): CartItem {
    return CartItem(
        id = id,
        productId = productId,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity,
        withExtraCheese = withExtraCheese,
        withExtraOnion = withExtraOnion
    )
}

fun CartItem.toEntity(): CartItemEntity {
    return CartItemEntity(
        id = id,
        productId = productId,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity,
        withExtraCheese = withExtraCheese,
        withExtraOnion = withExtraOnion
    )
}
