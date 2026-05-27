package com.example.data.entity

data class PaymentHistoryEntity(
    val id: Int = 0,
    val orderId: Int = 0,
    val customerName: String = "",
    val amountPaid: Double = 0.0,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "",
    val notes: String = ""
)
