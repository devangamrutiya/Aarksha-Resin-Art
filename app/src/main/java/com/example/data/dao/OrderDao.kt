package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Int): OrderEntity?

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderByIdFlow(id: Int): Flow<OrderEntity?>

    @Query("""
        SELECT * FROM orders 
        WHERE customerName LIKE '%' || :query || '%' 
        OR phoneNumber LIKE '%' || :query || '%' 
        OR productCategory LIKE '%' || :query || '%'
        ORDER BY createdDate DESC
    """)
    fun searchOrders(query: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Int)

    @Query("DELETE FROM orders WHERE phoneNumber = :phoneNumber")
    suspend fun deleteOrdersByPhoneNumber(phoneNumber: String)

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}
