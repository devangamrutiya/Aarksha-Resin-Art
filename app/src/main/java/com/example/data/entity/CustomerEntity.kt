package com.example.data.entity

data class CustomerEntity(
    val phoneNumber: String = "",
    val name: String = "",
    val note: String = "",
    val createdDate: Long = System.currentTimeMillis()
)
