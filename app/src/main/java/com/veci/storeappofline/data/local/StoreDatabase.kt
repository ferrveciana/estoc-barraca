package com.veci.storeappofline.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.veci.storeappofline.data.local.dao.CartItemDao
import com.veci.storeappofline.data.local.dao.ProductDao
import com.veci.storeappofline.data.local.entity.CartItemEntity
import com.veci.storeappofline.data.local.entity.ProductEntity

@Database(
    entities = [ProductEntity::class, CartItemEntity::class],
    version = 5,
    exportSchema = false
)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartItemDao(): CartItemDao

    companion object {
        val DUMMY_PRODUCTS = listOf(
            // Begudes típiques de barraques
            ProductEntity(id = 1, name = "Cervesa", price = 2.00, stockQuantity = 60, category = "BEVERAGE", emoji = "🍺"),
            ProductEntity(id = 2, name = "Coca-Cola", price = 2.50, stockQuantity = 40, category = "BEVERAGE", emoji = "🥤"),
            ProductEntity(id = 3, name = "Fanta Taronja", price = 2.50, stockQuantity = 35, category = "BEVERAGE", emoji = "🍊"),
            ProductEntity(id = 4, name = "Sprite", price = 2.50, stockQuantity = 30, category = "BEVERAGE", emoji = "🍋"),
            ProductEntity(id = 5, name = "Aigua Mineral", price = 1.50, stockQuantity = 50, category = "BEVERAGE", emoji = "💧"),
            ProductEntity(id = 6, name = "Roncola", price = 5.00, stockQuantity = 35, category = "BEVERAGE", emoji = "🍹"),
            ProductEntity(id = 7, name = "Cubata", price = 6.00, stockQuantity = 25, category = "BEVERAGE", emoji = "🥃"),
            ProductEntity(id = 8, name = "Kalimotxo", price = 3.50, stockQuantity = 40, category = "BEVERAGE", emoji = "🍷"),
            ProductEntity(id = 9, name = "Aquarius", price = 2.50, stockQuantity = 30, category = "BEVERAGE", emoji = "⚡"),
            ProductEntity(id = 10, name = "Cafè", price = 1.50, stockQuantity = 50, category = "BEVERAGE", emoji = "☕"),

            // Entrepans base (formatge i ceba com a suplements)
            ProductEntity(id = 11, name = "Entrepà de Llom", price = 4.50, stockQuantity = 40, category = "FOOD", isSandwich = true, emoji = "🥩"),
            ProductEntity(id = 12, name = "Entrepà de Bacon", price = 4.50, stockQuantity = 40, category = "FOOD", isSandwich = true, emoji = "🥓"),
            ProductEntity(id = 13, name = "Entrepà de Botifarra", price = 4.50, stockQuantity = 35, category = "FOOD", isSandwich = true, emoji = "🌭"),
            ProductEntity(id = 14, name = "Entrepà de Frankfurt", price = 4.00, stockQuantity = 40, category = "FOOD", isSandwich = true, emoji = "🌭"),
            ProductEntity(id = 15, name = "Entrepà de Pernil", price = 4.00, stockQuantity = 30, category = "FOOD", isSandwich = true, emoji = "🍖"),
            ProductEntity(id = 16, name = "Entrepà de Truita", price = 3.50, stockQuantity = 25, category = "FOOD", isSandwich = true, emoji = "🥚"),
            ProductEntity(id = 17, name = "Entrepà de Xoriço", price = 4.00, stockQuantity = 25, category = "FOOD", isSandwich = true, emoji = "🌶️"),

            // Altres menjars
            ProductEntity(id = 18, name = "Patates Fregides", price = 3.00, stockQuantity = 40, category = "FOOD", emoji = "🍟"),
            ProductEntity(id = 19, name = "Ració de Croquetes", price = 5.00, stockQuantity = 25, category = "FOOD", emoji = "🥟"),
            ProductEntity(id = 20, name = "Olives", price = 2.00, stockQuantity = 30, category = "FOOD", emoji = "🫒")
        )
    }
}
