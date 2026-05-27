package com.example.data.repository

import com.example.data.entity.CustomerEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.PaymentHistoryEntity
import com.example.data.model.CustomerWithStats
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class ArtRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ordersRef = db.collection("orders")
    private val customersRef = db.collection("customers")
    private val paymentsRef = db.collection("payments")

    val allOrders: Flow<List<OrderEntity>> = ordersRef
        .orderBy("createdDate", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot ->
            snapshot.documents.mapNotNull { it.toObject(OrderEntity::class.java) }
        }

    fun getOrderByIdFlow(id: Int): Flow<OrderEntity?> = ordersRef.document(id.toString())
        .snapshots()
        .map { it.toObject(OrderEntity::class.java) }

    suspend fun getOrderById(id: Int): OrderEntity? = ordersRef.document(id.toString())
        .get().await().toObject(OrderEntity::class.java)

    fun searchOrders(query: String): Flow<List<OrderEntity>> = allOrders.map { list ->
        if (query.isBlank()) list else list.filter {
            it.customerName.contains(query, ignoreCase = true) ||
            it.phoneNumber.contains(query, ignoreCase = true)
        }
    }

    suspend fun saveOrder(order: OrderEntity): Long {
        val normalizedPhone = order.phoneNumber.trim()
        if (normalizedPhone.isNotEmpty()) {
            val existingCustomer = customersRef.document(normalizedPhone).get().await().toObject(CustomerEntity::class.java)
            if (existingCustomer == null) {
                customersRef.document(normalizedPhone).set(
                    CustomerEntity(phoneNumber = normalizedPhone, name = order.customerName.trim())
                ).await()
            } else if (existingCustomer.name != order.customerName.trim()) {
                customersRef.document(normalizedPhone).set(
                    existingCustomer.copy(name = order.customerName.trim())
                ).await()
            }
        }
        
        var orderToSave = order
        val isNew = order.id == 0
        if (isNew) {
            val newId = kotlin.math.abs(Random.nextInt())
            orderToSave = order.copy(id = newId)
            ordersRef.document(newId.toString()).set(orderToSave).await()
            
            if (orderToSave.advancePaid > 0) {
                val newPaymentId = kotlin.math.abs(Random.nextInt())
                paymentsRef.document(newPaymentId.toString()).set(
                    PaymentHistoryEntity(
                        id = newPaymentId,
                        orderId = newId,
                        customerName = orderToSave.customerName,
                        amountPaid = orderToSave.advancePaid,
                        paymentMethod = "UPI",
                        notes = "Initial advance paid on creation"
                    )
                ).await()
            }
        } else {
            val oldOrder = getOrderById(order.id)
            if (oldOrder != null && oldOrder.advancePaid != order.advancePaid) {
                val diff = order.advancePaid - oldOrder.advancePaid
                if (diff > 0) {
                    val newPaymentId = kotlin.math.abs(Random.nextInt())
                    paymentsRef.document(newPaymentId.toString()).set(
                        PaymentHistoryEntity(
                            id = newPaymentId,
                            orderId = order.id,
                            customerName = order.customerName,
                            amountPaid = diff,
                            paymentMethod = "UPI",
                            notes = "Additional advance/partial payment registered"
                        )
                    ).await()
                }
            }
            ordersRef.document(order.id.toString()).set(orderToSave).await()
        }
        return orderToSave.id.toLong()
    }

    suspend fun deleteOrder(order: OrderEntity) {
        deleteOrderById(order.id)
    }

    suspend fun deleteOrderById(id: Int) {
        val payments = paymentsRef.whereEqualTo("orderId", id).get().await()
        for (doc in payments.documents) {
            doc.reference.delete().await()
        }
        ordersRef.document(id.toString()).delete().await()
    }

    fun getCustomersWithStats(query: String): Flow<List<CustomerWithStats>> {
        val customersFlow = customersRef.snapshots().map { snapshot -> 
            snapshot.documents.mapNotNull { it.toObject(CustomerEntity::class.java) }
        }
        return combine(customersFlow, allOrders) { customers, orders ->
            val result = customers.map { customer ->
                val customerOrders = orders.filter { it.phoneNumber == customer.phoneNumber }
                CustomerWithStats(
                    phoneNumber = customer.phoneNumber,
                    name = customer.name,
                    totalOrdersCount = customerOrders.size,
                    totalSpent = customerOrders.sumOf { it.totalAmount },
                    lastOrderDate = customerOrders.maxOfOrNull { it.createdDate }
                )
            }
            if (query.isBlank()) result else result.filter {
                it.name.contains(query, ignoreCase = true) || it.phoneNumber.contains(query, ignoreCase = true)
            }
        }
    }

    suspend fun deleteCustomerAndAllOrders(phoneNumber: String) {
        val orders = ordersRef.whereEqualTo("phoneNumber", phoneNumber).get().await()
        for (doc in orders.documents) {
            val orderId = doc.getLong("id")?.toInt()
            if (orderId != null) deleteOrderById(orderId)
        }
        customersRef.document(phoneNumber).delete().await()
    }

    val allPayments: Flow<List<PaymentHistoryEntity>> = paymentsRef
        .orderBy("paymentDate", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot ->
            snapshot.documents.mapNotNull { it.toObject(PaymentHistoryEntity::class.java) }
        }

    fun searchPayments(query: String): Flow<List<PaymentHistoryEntity>> = allPayments.map { list ->
        if (query.isBlank()) list else list.filter {
            it.customerName.contains(query, ignoreCase = true) ||
            it.notes.contains(query, ignoreCase = true)
        }
    }

    suspend fun addPaymentHistory(payment: PaymentHistoryEntity): Long {
        val paymentToSave = if (payment.id == 0) payment.copy(id = kotlin.math.abs(Random.nextInt())) else payment
        paymentsRef.document(paymentToSave.id.toString()).set(paymentToSave).await()
        return paymentToSave.id.toLong()
    }

    suspend fun recordManualPayment(orderId: Int, amount: Double, method: String, notes: String) {
        val order = getOrderById(orderId)
        if (order != null) {
            val newAdvance = order.advancePaid + amount
            val newPaymentStatus = when {
                newAdvance >= order.totalAmount -> "Settled"
                newAdvance > 0 -> "Partial"
                else -> "Pending"
            }
            
            val updatedOrder = order.copy(
                advancePaid = newAdvance.coerceAtMost(order.totalAmount),
                paymentStatus = newPaymentStatus
            )
            ordersRef.document(orderId.toString()).set(updatedOrder).await()
            
            addPaymentHistory(
                PaymentHistoryEntity(
                    orderId = orderId,
                    customerName = order.customerName,
                    amountPaid = amount,
                    paymentMethod = method,
                    notes = notes
                )
            )
        }
    }

    suspend fun markPaymentAsSettled(orderId: Int, paymentMethod: String = "UPI") {
        val order = getOrderById(orderId)
        if (order != null && order.paymentStatus != "Settled") {
            val outstanding = order.totalAmount - order.advancePaid
            if (outstanding > 0) {
                addPaymentHistory(
                    PaymentHistoryEntity(
                        orderId = order.id,
                        customerName = order.customerName,
                        amountPaid = outstanding,
                        paymentMethod = paymentMethod,
                        notes = "Balance settled in full"
                    )
                )
            }
            val updatedOrder = order.copy(
                paymentStatus = "Settled",
                advancePaid = order.totalAmount
            )
            ordersRef.document(orderId.toString()).set(updatedOrder).await()
        }
    }

    suspend fun getAllCustomers(): List<CustomerEntity> {
        return customersRef.get().await().toObjects(CustomerEntity::class.java)
    }

    suspend fun restoreDatabase(customers: List<CustomerEntity>, orders: List<OrderEntity>, payments: List<PaymentHistoryEntity>) {
        // Warning: This does not delete existing data on Firebase.
        // Doing a full delete could be dangerous, so we'll just upsert.
        for (c in customers) {
            customersRef.document(c.phoneNumber).set(c).await()
        }
        for (o in orders) {
            ordersRef.document(o.id.toString()).set(o).await()
        }
        for (p in payments) {
            paymentsRef.document(p.id.toString()).set(p).await()
        }
    }
}

