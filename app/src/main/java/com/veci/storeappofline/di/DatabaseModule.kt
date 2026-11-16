package com.veci.storeappofline.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.veci.storeappofline.data.local.StoreDatabase
import com.veci.storeappofline.data.local.dao.CartItemDao
import com.veci.storeappofline.data.local.dao.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private lateinit var storeDatabase: StoreDatabase

    @Provides
    @Singleton
    fun provideStoreDatabase(
        @ApplicationContext context: Context
    ): StoreDatabase {
        storeDatabase = Room.databaseBuilder(
            context,
            StoreDatabase::class.java,
            "store_database"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedDatabaseIfEmpty()
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    seedDatabaseIfEmpty()
                }

                private fun seedDatabaseIfEmpty() {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (::storeDatabase.isInitialized) {
                                val products = storeDatabase.productDao().getProductsOnce()
                                if (products.isEmpty()) {
                                    storeDatabase.productDao().insertProducts(StoreDatabase.DUMMY_PRODUCTS)
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
        return storeDatabase
    }

    @Provides
    fun provideProductDao(database: StoreDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideCartItemDao(database: StoreDatabase): CartItemDao {
        return database.cartItemDao()
    }
}
