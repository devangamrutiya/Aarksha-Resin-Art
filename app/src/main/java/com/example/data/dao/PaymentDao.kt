package com.example.data.dao

import androidx.room.*
import com.example.data.entity.PaymentHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment_history ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentHistoryEntity>>

    @Query("SELECT * FROM payment_history WHERE orderId = :orderId ORDER BY paymentDate DESC")
    fun getPaymentsForOrder(orderId: Int): Flow<List<PaymentHistoryEntity>>

    @Query("""
        SELECT * FROM payment_history 
        WHERE customerName LIKE '%' || :query || '%' 
        OR paymentMethod LIKE '%' || :query || '%'
        OR notes LIKE '%' || :query || '%'
        ORDER BY paymentDate DESC
    """)
    fun searchPayments(query: String): Flow<List<PaymentHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentHistoryEntity): Long

    @Delete
    suspend fun deletePayment(payment: PaymentHistoryEntity)

    @Query("DELETE FROM payment_history WHERE orderId = :orderId")
    suspend fun deletePaymentsForOrder(orderId: Int)

    @Query("DELETE FROM payment_history")
    suspend fun deleteAllPayments()
}
