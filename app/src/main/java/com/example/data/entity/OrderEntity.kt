package com.example.data.entity

data class OrderEntity(
    val id: Int = 0,
    val customerName: String = "",
    val phoneNumber: String = "",
    val productCategory: String = "", 
    val size: String = "",            
    val description: String = "",
    val totalAmount: Double = 0.0,
    val advancePaid: Double = 0.0,
    val paymentStatus: String = "",   
    val orderStatus: String = "",     
    val deliveryDate: Long = 0L,      
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis()
)
