package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.entity.CustomerEntity
import com.example.data.model.CustomerWithStats
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("""
        SELECT 
            c.phoneNumber AS phoneNumber, 
            c.name AS name, 
            COUNT(o.id) AS totalOrdersCount, 
            SUM(o.totalAmount) AS totalSpent, 
            MAX(o.createdDate) AS lastOrderDate
        FROM customers c
        LEFT JOIN orders o ON c.phoneNumber = o.phoneNumber
        WHERE c.name LIKE '%' || :query || '%' OR c.phoneNumber LIKE '%' || :query || '%'
        GROUP BY c.phoneNumber, c.name
        ORDER BY totalSpent DESC
    """)
    fun getCustomersWithStats(query: String): Flow<List<CustomerWithStats>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE phoneNumber = :phoneNumber")
    suspend fun deleteCustomerByPhone(phoneNumber: String)

    @Query("SELECT * FROM customers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getCustomerByPhone(phoneNumber: String): CustomerEntity?

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}
