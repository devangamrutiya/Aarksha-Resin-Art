package com.example.data.model

data class CustomerWithStats(
    val phoneNumber: String,
    val name: String,
    val totalOrdersCount: Int,
    val totalSpent: Double?,
    val lastOrderDate: Long?
) {
    val totalSpentSafe: Double get() = totalSpent ?: 0.0
    val lastOrderDateSafe: Long get() = lastOrderDate ?: 0L
}
