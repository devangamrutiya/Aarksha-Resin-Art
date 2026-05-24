package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val phoneNumber: String,
    val productCategory: String, // e.g., "Wall Clock", "Coasters", "Geode Art", "Resin Table"
    val size: String,            // e.g., "12 inch", "18 inch", "Custom"
    val description: String,
    val totalAmount: Double,
    val advancePaid: Double,
    val paymentStatus: String,   // "Pending", "Partial", "Settled"
    val orderStatus: String,     // "Not Started", "In Progress", "Completed", "Cancelled"
    val deliveryDate: Long,      // timestamp in ms
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis()
)
