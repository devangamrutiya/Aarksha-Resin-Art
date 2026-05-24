package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_history")
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val customerName: String,
    val amountPaid: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String, // "Cash", "UPI", "Card", "Bank Transfer"
    val notes: String = ""
)
