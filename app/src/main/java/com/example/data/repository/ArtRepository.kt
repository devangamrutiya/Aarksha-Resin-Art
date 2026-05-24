package com.example.data.repository

import com.example.data.dao.CustomerDao
import com.example.data.dao.OrderDao
import com.example.data.dao.PaymentDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.PaymentHistoryEntity
import com.example.data.model.CustomerWithStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ArtRepository(
    private val orderDao: OrderDao,
    private val customerDao: CustomerDao,
    private val paymentDao: PaymentDao
) {
    // Orders
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    
    fun getOrderByIdFlow(id: Int): Flow<OrderEntity?> = orderDao.getOrderByIdFlow(id)
    
    suspend fun getOrderById(id: Int): OrderEntity? = orderDao.getOrderById(id)

    fun searchOrders(query: String): Flow<List<OrderEntity>> = orderDao.searchOrders(query)

    suspend fun saveOrder(order: OrderEntity): Long {
        // Automatically check if customer profile exists, if not create it
        val normalizedPhone = order.phoneNumber.trim()
        if (normalizedPhone.isNotEmpty()) {
            val existingCustomer = customerDao.getCustomerByPhone(normalizedPhone)
            if (existingCustomer == null) {
                customerDao.insertCustomer(
                    CustomerEntity(
                        phoneNumber = normalizedPhone,
                        name = order.customerName.trim()
                    )
                )
            } else if (existingCustomer.name != order.customerName.trim()) {
                // Keep name updated
                customerDao.insertCustomer(
                    existingCustomer.copy(name = order.customerName.trim())
                )
            }
        }
        
        val orderId = if (order.id == 0) {
            val newId = orderDao.insertOrder(order)
            // If advance is paid, log that payment in transaction log!
            if (order.advancePaid > 0) {
                paymentDao.insertPayment(
                    PaymentHistoryEntity(
                        orderId = newId.toInt(),
                        customerName = order.customerName,
                        amountPaid = order.advancePaid,
                        paymentMethod = "UPI", // standard default
                        notes = "Initial advance paid on creation"
                    )
                )
            }
            newId
        } else {
            val oldOrder = orderDao.getOrderById(order.id)
            if (oldOrder != null && oldOrder.advancePaid != order.advancePaid) {
                val diff = order.advancePaid - oldOrder.advancePaid
                if (diff > 0) {
                    paymentDao.insertPayment(
                        PaymentHistoryEntity(
                            orderId = order.id,
                            customerName = order.customerName,
                            amountPaid = diff,
                            paymentMethod = "UPI",
                            notes = "Additional advance/partial payment registered"
                        )
                    )
                }
            }
            orderDao.updateOrder(order)
            order.id.toLong()
        }
        return orderId
    }

    suspend fun deleteOrder(order: OrderEntity) {
        paymentDao.deletePaymentsForOrder(order.id)
        orderDao.deleteOrder(order)
    }

    suspend fun deleteOrderById(id: Int) {
        paymentDao.deletePaymentsForOrder(id)
        orderDao.deleteOrderById(id)
    }

    // Customers with aggregates
    fun getCustomersWithStats(query: String): Flow<List<CustomerWithStats>> {
        return customerDao.getCustomersWithStats(query)
    }

    suspend fun deleteCustomerAndAllOrders(phoneNumber: String) {
        orderDao.deleteOrdersByPhoneNumber(phoneNumber)
        customerDao.deleteCustomerByPhone(phoneNumber)
    }

    // Payments Tracker and History
    val allPayments: Flow<List<PaymentHistoryEntity>> = paymentDao.getAllPayments()

    fun searchPayments(query: String): Flow<List<PaymentHistoryEntity>> = paymentDao.searchPayments(query)

    suspend fun addPaymentHistory(payment: PaymentHistoryEntity): Long {
        return paymentDao.insertPayment(payment)
    }

    suspend fun recordManualPayment(orderId: Int, amount: Double, method: String, notes: String) {
        val order = orderDao.getOrderById(orderId)
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
            orderDao.updateOrder(updatedOrder)
            
            paymentDao.insertPayment(
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
        val order = orderDao.getOrderById(orderId)
        if (order != null && order.paymentStatus != "Settled") {
            val outstanding = order.totalAmount - order.advancePaid
            if (outstanding > 0) {
                paymentDao.insertPayment(
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
            orderDao.updateOrder(updatedOrder)
        }
    }
}
