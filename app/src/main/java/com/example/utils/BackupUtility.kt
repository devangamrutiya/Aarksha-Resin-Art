package com.example.utils

import com.example.data.entity.CustomerEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.PaymentHistoryEntity
import org.json.JSONArray
import org.json.JSONObject

data class BackupData(
    val orders: List<OrderEntity>,
    val customers: List<CustomerEntity>,
    val payments: List<PaymentHistoryEntity>
)

object BackupUtility {

    fun exportDatabaseToJson(
        orders: List<OrderEntity>,
        customers: List<CustomerEntity>,
        payments: List<PaymentHistoryEntity>
    ): String {
        val root = JSONObject()

        // 1. Serialize Orders
        val ordersArray = JSONArray()
        for (order in orders) {
            val orderObj = JSONObject()
            orderObj.put("id", order.id)
            orderObj.put("customerName", order.customerName)
            orderObj.put("phoneNumber", order.phoneNumber)
            orderObj.put("productCategory", order.productCategory)
            orderObj.put("size", order.size)
            orderObj.put("description", order.description)
            orderObj.put("totalAmount", order.totalAmount)
            orderObj.put("advancePaid", order.advancePaid)
            orderObj.put("paymentStatus", order.paymentStatus)
            orderObj.put("orderStatus", order.orderStatus)
            orderObj.put("deliveryDate", order.deliveryDate)
            orderObj.put("notes", order.notes)
            orderObj.put("createdDate", order.createdDate)
            ordersArray.put(orderObj)
        }
        root.put("orders", ordersArray)

        // 2. Serialize Customers
        val customersArray = JSONArray()
        for (customer in customers) {
            val customerObj = JSONObject()
            customerObj.put("phoneNumber", customer.phoneNumber)
            customerObj.put("name", customer.name)
            customerObj.put("note", customer.note)
            customerObj.put("createdDate", customer.createdDate)
            customersArray.put(customerObj)
        }
        root.put("customers", customersArray)

        // 3. Serialize Payments
        val paymentsArray = JSONArray()
        for (payment in payments) {
            val paymentObj = JSONObject()
            paymentObj.put("id", payment.id)
            paymentObj.put("orderId", payment.orderId)
            paymentObj.put("customerName", payment.customerName)
            paymentObj.put("amountPaid", payment.amountPaid)
            paymentObj.put("paymentDate", payment.paymentDate)
            paymentObj.put("paymentMethod", payment.paymentMethod)
            paymentObj.put("notes", payment.notes)
            paymentsArray.put(paymentObj)
        }
        root.put("payments", paymentsArray)

        return root.toString(4) // Beautifully indented JSON
    }

    fun parseBackupJson(jsonString: String): BackupData? {
        return try {
            val root = JSONObject(jsonString)

            // 1. Parse Orders
            val ordersList = mutableListOf<OrderEntity>()
            if (root.has("orders")) {
                val array = root.getJSONArray("orders")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    ordersList.add(
                        OrderEntity(
                            id = obj.optInt("id", 0),
                            customerName = obj.optString("customerName", ""),
                            phoneNumber = obj.optString("phoneNumber", ""),
                            productCategory = obj.optString("productCategory", "Square"),
                            size = obj.optString("size", ""),
                            description = obj.optString("description", ""),
                            totalAmount = obj.optDouble("totalAmount", 0.0),
                            advancePaid = obj.optDouble("advancePaid", 0.0),
                            paymentStatus = obj.optString("paymentStatus", "Pending"),
                            orderStatus = obj.optString("orderStatus", "Not Started"),
                            deliveryDate = obj.optLong("deliveryDate", System.currentTimeMillis()),
                            notes = obj.optString("notes", ""),
                            createdDate = obj.optLong("createdDate", System.currentTimeMillis())
                        )
                    )
                }
            }

            // 2. Parse Customers
            val customersList = mutableListOf<CustomerEntity>()
            if (root.has("customers")) {
                val array = root.getJSONArray("customers")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    customersList.add(
                        CustomerEntity(
                            phoneNumber = obj.optString("phoneNumber", ""),
                            name = obj.optString("name", "Unknown"),
                            note = obj.optString("note", ""),
                            createdDate = obj.optLong("createdDate", System.currentTimeMillis())
                        )
                    )
                }
            }

            // 3. Parse Payments
            val paymentsList = mutableListOf<PaymentHistoryEntity>()
            if (root.has("payments")) {
                val array = root.getJSONArray("payments")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    paymentsList.add(
                        PaymentHistoryEntity(
                            id = obj.optInt("id", 0),
                            orderId = obj.optInt("orderId", 0),
                            customerName = obj.optString("customerName", ""),
                            amountPaid = obj.optDouble("amountPaid", 0.0),
                            paymentDate = obj.optLong("paymentDate", System.currentTimeMillis()),
                            paymentMethod = obj.optString("paymentMethod", "Cash"),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            BackupData(ordersList, customersList, paymentsList)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportOrdersToCsv(orders: List<OrderEntity>): String {
        val csv = StringBuilder()
        // Headers
        csv.append("Order ID,Customer Name,Phone Number,Product Category,Size,Description,Total Amount,Advance Paid,Remaining Balance,Payment Status,Order Status,Delivery Date,Created Date,Notes\n")
        
        for (o in orders) {
            val remaining = o.totalAmount - o.advancePaid
            // Clean values from comma conflict
            val name = escapeCsv(o.customerName)
            val category = escapeCsv(o.productCategory)
            val size = escapeCsv(o.size)
            val desc = escapeCsv(o.description)
            val notes = escapeCsv(o.notes)
            
            val deliveryDateStr = formatDate(o.deliveryDate)
            val createdDateStr = formatDate(o.createdDate)

            csv.append("${o.id},$name,${o.phoneNumber},$category,$size,$desc,${o.totalAmount},${o.advancePaid},$remaining,${o.paymentStatus},${o.orderStatus},$deliveryDateStr,$createdDateStr,$notes\n")
        }
        return csv.toString()
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\"", "\"\"")
        return if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            "\"$clean\""
        } else {
            clean
        }
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            sdf.format(java.util.Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
}
