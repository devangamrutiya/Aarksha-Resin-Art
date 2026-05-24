package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val phoneNumber: String,
    val name: String,
    val note: String = "",
    val createdDate: Long = System.currentTimeMillis()
)
