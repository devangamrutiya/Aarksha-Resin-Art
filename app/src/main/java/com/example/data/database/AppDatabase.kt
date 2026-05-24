package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CustomerDao
import com.example.data.dao.OrderDao
import com.example.data.dao.PaymentDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.PaymentHistoryEntity

@Database(
    entities = [
        OrderEntity::class,
        CustomerEntity::class,
        PaymentHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun customerDao(): CustomerDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aaraksha_resin_art_db"
                )
                .fallbackToDestructiveMigration() // safe for clean init / updates
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
