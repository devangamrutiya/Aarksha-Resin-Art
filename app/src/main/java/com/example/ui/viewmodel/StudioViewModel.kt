package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import androidx.room.withTransaction
import com.example.data.entity.CustomerEntity
import com.example.data.entity.OrderEntity
import com.example.data.entity.PaymentHistoryEntity
import com.example.data.model.CustomerWithStats
import com.example.data.repository.ArtRepository
import com.example.data.repository.PreferencesRepository
import com.example.utils.BackupUtility
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudioViewModel(
    application: Application,
    private val artRepository: ArtRepository,
    private val preferencesRepository: PreferencesRepository
) : AndroidViewModel(application) {

    // --- SECURITY & PASSCODE STATES ---
    val passcode: StateFlow<String> = preferencesRepository.passcodeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "1234")

    val passcodeEnabled: StateFlow<Boolean> = preferencesRepository.passcodeEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val biometricEnabled: StateFlow<Boolean> = preferencesRepository.biometricEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val darkMode: StateFlow<String> = preferencesRepository.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _inputPin = MutableStateFlow("")
    val inputPin: StateFlow<String> = _inputPin.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    fun enterPinDigit(digit: Char) {
        if (_inputPin.value.length < 4) {
            _inputPin.value += digit
            _pinError.value = null
            if (_inputPin.value.length == 4) {
                verifyPin()
            }
        }
    }

    fun deletePinDigit() {
        if (_inputPin.value.isNotEmpty()) {
            _inputPin.value = _inputPin.value.dropLast(1)
        }
    }

    fun clearPin() {
        _inputPin.value = ""
        _pinError.value = null
    }

    private fun verifyPin() {
        viewModelScope.launch {
            val correctPin = preferencesRepository.passcodeFlow.first()
            if (_inputPin.value == correctPin) {
                _isAuthenticated.value = true
                _pinError.value = null
            } else {
                _inputPin.value = ""
                _pinError.value = "Incorrect passcode. Try again."
            }
        }
    }

    fun lockSession() {
        _isAuthenticated.value = false
        _inputPin.value = ""
    }

    fun updatePasscode(newPasscode: String) {
        viewModelScope.launch {
            if (newPasscode.length == 4 && newPasscode.all { it.isDigit() }) {
                preferencesRepository.savePasscode(newPasscode)
            }
        }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.saveDarkMode(mode)
        }
    }

    fun setPasscodeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.savePasscodeEnabled(enabled)
            if (!enabled) {
                _isAuthenticated.value = true
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveBiometricEnabled(enabled)
        }
    }

    fun authenticateSuccessfully() {
        _isAuthenticated.value = true
        _inputPin.value = ""
        _pinError.value = null
    }


    // --- ORDERS MANAGEMENT Flow ---
    private val _orderSearchQuery = MutableStateFlow("")
    val orderSearchQuery: StateFlow<String> = _orderSearchQuery.asStateFlow()

    private val _orderStatusFilter = MutableStateFlow("All") // "All", "Not Started", "In Progress", "Completed", "Cancelled"
    val orderStatusFilter: StateFlow<String> = _orderStatusFilter.asStateFlow()

    val allOrders: StateFlow<List<OrderEntity>> = artRepository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredOrders: StateFlow<List<OrderEntity>> = combine(
        allOrders,
        _orderSearchQuery,
        _orderStatusFilter
    ) { orders, query, filterStatus ->
        var res = orders
        if (query.isNotEmpty()) {
            res = res.filter {
                it.customerName.contains(query, ignoreCase = true) ||
                it.phoneNumber.contains(query) ||
                it.productCategory.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
        if (filterStatus != "All") {
            res = res.filter { it.orderStatus == filterStatus }
        }
        res
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateOrderSearchQuery(query: String) {
        _orderSearchQuery.value = query
    }

    fun updateOrderStatusFilter(filter: String) {
        _orderStatusFilter.value = filter
    }

    fun saveOrder(order: OrderEntity) {
        viewModelScope.launch {
            artRepository.saveOrder(order)
        }
    }

    fun quickCompleteOrder(orderId: Int) {
        viewModelScope.launch {
            val order = artRepository.getOrderById(orderId)
            if (order != null) {
                artRepository.saveOrder(
                    order.copy(orderStatus = "Completed")
                )
            }
        }
    }

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            artRepository.deleteOrder(order)
        }
    }


    // --- CLIENTS / CUSTOMERS FLOW ---
    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    val customersWithStats: StateFlow<List<CustomerWithStats>> = _customerSearchQuery
        .flatMapLatest { query ->
            artRepository.getCustomersWithStats(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateCustomerSearchQuery(query: String) {
        _customerSearchQuery.value = query
    }

    fun deleteCustomer(phoneNumber: String) {
        viewModelScope.launch {
            artRepository.deleteCustomerAndAllOrders(phoneNumber)
        }
    }


    // --- PAYMENTS FLOW ---
    private val _paymentSearchQuery = MutableStateFlow("")
    val paymentSearchQuery: StateFlow<String> = _paymentSearchQuery.asStateFlow()

    private val _paymentStatusFilter = MutableStateFlow("All") // "All", "Pending", "Partial", "Settled"
    val paymentStatusFilter: StateFlow<String> = _paymentStatusFilter.asStateFlow()

    val paymentsHistory: StateFlow<List<PaymentHistoryEntity>> = artRepository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPayments: StateFlow<List<OrderEntity>> = combine(
        allOrders,
        _paymentSearchQuery,
        _paymentStatusFilter
    ) { orders, query, filter ->
        var res = orders
        if (query.isNotEmpty()) {
            res = res.filter {
                it.customerName.contains(query, ignoreCase = true) ||
                it.phoneNumber.contains(query)
            }
        }
        if (filter != "All") {
            res = res.filter { it.paymentStatus == filter }
        }
        res
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updatePaymentSearchQuery(query: String) {
        _paymentSearchQuery.value = query
    }

    fun updatePaymentStatusFilter(filter: String) {
        _paymentStatusFilter.value = filter
    }

    fun recordManualPayment(orderId: Int, amount: Double, method: String, notes: String) {
        viewModelScope.launch {
            artRepository.recordManualPayment(orderId, amount, method, notes)
        }
    }

    fun markPaymentAsSettled(orderId: Int, method: String = "UPI") {
        viewModelScope.launch {
            artRepository.markPaymentAsSettled(orderId, method)
        }
    }


    // --- ANALYTICS & STATS ---
    val kpiStats: StateFlow<KPIValues> = allOrders.map { orders ->
        val progress = orders.count { it.orderStatus == "In Progress" }
        val pending = orders.count { it.paymentStatus == "Pending" || it.paymentStatus == "Partial" }
        val settledCount = orders.count { it.paymentStatus == "Settled" }
        val completed = orders.count { it.orderStatus == "Completed" }
        
        // Total revenue definition: aggregate sum of advance paid across all active order entries
        val totalRevenue = orders.sumOf { it.advancePaid }
        val totalPendingPaymentsVal = orders.sumOf { (it.totalAmount - it.advancePaid).coerceAtLeast(0.0) }

        KPIValues(
            ordersInProgress = progress,
            paymentPendingCount = pending,
            paymentsSettledCount = settledCount,
            totalCustomersCount = orders.map { it.phoneNumber }.distinct().size,
            completedOrders = completed,
            totalRevenue = totalRevenue,
            totalPendingPaymentsAmount = totalPendingPaymentsVal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KPIValues())


    // --- BACKUP & EXPORT ---
    fun getDatabaseBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            // Get data synchronously from reactive flows
            val currentOrders = allOrders.value
            val currentPayments = paymentsHistory.value
            val currentCustomers = AppDatabase.getDatabase(getApplication()).customerDao().getAllCustomers().firstOrNull() ?: emptyList()
            
            val jsonString = BackupUtility.exportDatabaseToJson(
                orders = currentOrders,
                customers = currentCustomers,
                payments = currentPayments
            )
            onResult(jsonString)
        }
    }

    fun getOrdersCsv(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val currentOrders = allOrders.value
            val csv = BackupUtility.exportOrdersToCsv(currentOrders)
            onResult(csv)
        }
    }

    fun restoreDatabaseFromJson(jsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val parsed = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                BackupUtility.parseBackupJson(jsonString)
            }
            if (parsed == null) {
                onResult(false, "Invalid or corrupted backup file format.")
                return@launch
            }

            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(getApplication())
                    val orderDao = db.orderDao()
                    val customerDao = db.customerDao()
                    val paymentDao = db.paymentDao()

                    // Execute restore transactionally to support rollback if insertion of any element fails
                    db.withTransaction {
                        paymentDao.deleteAllPayments()
                        orderDao.deleteAllOrders()
                        customerDao.deleteAllCustomers()

                        // Insert Customers
                        for (cust in parsed.customers) {
                            customerDao.insertCustomer(cust)
                        }
                        // Insert Orders
                        for (ord in parsed.orders) {
                            orderDao.insertOrder(ord)
                        }
                        // Insert Payments
                        for (pay in parsed.payments) {
                            paymentDao.insertPayment(pay)
                        }
                    }
                }

                onResult(true, "Database backup restored successfully! Loaded ${parsed.orders.size} orders and ${parsed.customers.size} customer profiles.")
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, "Error occurred during restore: ${e.localizedMessage}")
            }
        }
    }
}

data class KPIValues(
    val ordersInProgress: Int = 0,
    val paymentPendingCount: Int = 0,
    val paymentsSettledCount: Int = 0,
    val totalCustomersCount: Int = 0,
    val completedOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalPendingPaymentsAmount: Double = 0.0
)

class StudioViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudioViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val repository = ArtRepository(database.orderDao(), database.customerDao(), database.paymentDao())
            val preferences = PreferencesRepository(application)
            @Suppress("UNCHECKED_CAST")
            return StudioViewModel(application, repository, preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
