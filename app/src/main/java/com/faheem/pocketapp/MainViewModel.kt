package com.faheem.pocketapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.faheem.pocketapp.data.repository.AuthRepository
import com.faheem.pocketapp.data.repository.AuthRepositoryImpl
import com.faheem.pocketapp.data.repository.EventRepository
import com.faheem.pocketapp.data.repository.EventRepositoryImpl
import com.faheem.pocketapp.data.repository.ExpenseRepository
import com.faheem.pocketapp.data.repository.ExpenseRepositoryImpl
import com.faheem.pocketapp.data.repository.PaymentRepository
import com.faheem.pocketapp.data.repository.PaymentRepositoryImpl
import com.faheem.pocketapp.data.repository.TaskRepository
import com.faheem.pocketapp.data.repository.TaskRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class TaskItem(
    val id: String,
    val title: String,
    val details: String,
    val scheduledAtMillis: Long,
    val alarmEnabled: Boolean,
    val updatedAt: Long
)

data class ExpenseItem(
    val id: String,
    val title: String,
    val amount: Double,
    val currency: String = "USD",
    val category: String,
    val paymentMethod: String,
    val notes: String,
    val scheduledAtMillis: Long,
    val alarmEnabled: Boolean,
    val updatedAt: Long
)

data class EventItem(
    val id: String,
    val title: String,
    val description: String,
    val eventDateMillis: Long,
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val alarmEnabled: Boolean,
    val updatedAt: Long
)

data class PaymentItem(
    val id: String,
    val title: String,
    val amount: Double,
    val currency: String = "USD",
    val paymentType: String, // "have_to_take" or "have_to_give"
    val description: String,
    val scheduledAtMillis: Long,
    val alarmEnabled: Boolean,
    val updatedAt: Long,
    val isFuturePayment: Boolean = true
)

data class PocketUiState(
    val isLoading: Boolean = false,
    val currentUserEmail: String? = null,
    val tasks: List<TaskItem> = emptyList(),
    val expenses: List<ExpenseItem> = emptyList(),
    val events: List<EventItem> = emptyList(),
    val payments: List<PaymentItem> = emptyList(),
    val errorMessage: String? = null
)

class MainViewModel(
    application: Application,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val taskRepository: TaskRepository = TaskRepositoryImpl(),
    private val expenseRepository: ExpenseRepository = ExpenseRepositoryImpl(),
    private val eventRepository: EventRepository = EventRepositoryImpl(),
    private val paymentRepository: PaymentRepository = PaymentRepositoryImpl()
) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var userDataJob: Job? = null

    private val _uiState = MutableStateFlow(PocketUiState())
    val uiState: StateFlow<PocketUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { email ->
                if (email != null) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        _uiState.value = _uiState.value.copy(currentUserEmail = email)
                        observeUserData(uid)
                    }
                } else {
                    userDataJob?.cancel()
                    _uiState.value = PocketUiState()
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (!validateCredentials(email, password)) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.login(email, password)
                .onSuccess { clearError() }
                .onFailure { setError(it.message ?: "Login failed") }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun register(email: String, password: String) {
        if (!validateCredentials(email, password)) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.register(email, password)
                .onSuccess { clearError() }
                .onFailure { setError(it.message ?: "Registration failed") }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun signOut() {
        authRepository.logout()
    }

    fun addTask(title: String, details: String, scheduledAtMillis: Long, alarmEnabled: Boolean) {
        if (title.isBlank()) {
            setError("Task title is required.")
            return
        }

        viewModelScope.launch {
            taskRepository.addTask(title, details, scheduledAtMillis, alarmEnabled)
                .onSuccess { itemId ->
                    if (alarmEnabled) {
                        AlarmScheduler.scheduleReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_TASK,
                            itemId = itemId,
                            title = title.trim(),
                            scheduledAtMillis = scheduledAtMillis
                        )
                    }
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to save task.") }
        }
    }

    fun updateTask(item: TaskItem) {
        if (item.title.isBlank()) {
            setError("Task title is required.")
            return
        }

        viewModelScope.launch {
            taskRepository.updateTask(item)
                .onSuccess {
                    if (item.alarmEnabled) {
                        AlarmScheduler.scheduleReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_TASK,
                            itemId = item.id,
                            title = item.title,
                            scheduledAtMillis = item.scheduledAtMillis
                        )
                    } else {
                        AlarmScheduler.cancelReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_TASK,
                            itemId = item.id
                        )
                    }
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to update task.") }
        }
    }

    fun deleteTask(item: TaskItem) {
        viewModelScope.launch {
            taskRepository.deleteTask(item)
                .onSuccess {
                    AlarmScheduler.cancelReminder(
                        context = getApplication(),
                        module = AlarmScheduler.MODULE_TASK,
                        itemId = item.id
                    )
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to delete task.") }
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        currency: String,
        category: String,
        paymentMethod: String,
        notes: String,
        scheduledAtMillis: Long,
        alarmEnabled: Boolean
    ) {
        if (title.isBlank()) {
            setError("Expense title is required.")
            return
        }
        if (amount <= 0.0) {
            setError("Expense amount must be greater than 0.")
            return
        }

        viewModelScope.launch {
            expenseRepository.addExpense(title, amount, currency, category, paymentMethod, notes, scheduledAtMillis, alarmEnabled)
                .onSuccess { itemId ->
                    if (alarmEnabled) {
                        AlarmScheduler.scheduleReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_EXPENSE,
                            itemId = itemId,
                            title = title.trim(),
                            scheduledAtMillis = scheduledAtMillis
                        )
                    }
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to save expense.") }
        }
    }

    fun updateExpense(item: ExpenseItem) {
        if (item.title.isBlank()) {
            setError("Expense title is required.")
            return
        }

        viewModelScope.launch {
            expenseRepository.updateExpense(item)
                .onSuccess {
                    if (item.alarmEnabled) {
                        AlarmScheduler.scheduleReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_EXPENSE,
                            itemId = item.id,
                            title = item.title,
                            scheduledAtMillis = item.scheduledAtMillis
                        )
                    } else {
                        AlarmScheduler.cancelReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_EXPENSE,
                            itemId = item.id
                        )
                    }
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to update expense.") }
        }
    }

    fun deleteExpense(item: ExpenseItem) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(item)
                .onSuccess {
                    AlarmScheduler.cancelReminder(
                        context = getApplication(),
                        module = AlarmScheduler.MODULE_EXPENSE,
                        itemId = item.id
                    )
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to delete expense.") }
        }
    }

    fun addEvent(
        title: String,
        description: String,
        eventDateMillis: Long,
        locationName: String,
        latitude: Double?,
        longitude: Double?,
        alarmEnabled: Boolean
    ) {
        if (title.isBlank()) {
            setError("Event title is required.")
            return
        }

        viewModelScope.launch {
            eventRepository.addEvent(
                title = title,
                description = description,
                eventDateMillis = eventDateMillis,
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                alarmEnabled = alarmEnabled
            )
                .onSuccess { itemId ->
                    if (alarmEnabled) {
                        AlarmScheduler.scheduleReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_EVENT,
                            itemId = itemId,
                            title = title.trim(),
                            scheduledAtMillis = eventDateMillis
                        )
                    }
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to save event.") }
        }
    }

    fun updateEvent(item: EventItem) {
        if (item.title.isBlank()) {
            setError("Event title is required.")
            return
        }

        viewModelScope.launch {
            eventRepository.updateEvent(item)
                .onSuccess {
                    if (item.alarmEnabled) {
                        AlarmScheduler.scheduleReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_EVENT,
                            itemId = item.id,
                            title = item.title,
                            scheduledAtMillis = item.eventDateMillis
                        )
                    } else {
                        AlarmScheduler.cancelReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_EVENT,
                            itemId = item.id
                        )
                    }
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to update event.") }
        }
    }

    fun deleteEvent(item: EventItem) {
        viewModelScope.launch {
            eventRepository.deleteEvent(item)
                .onSuccess {
                    AlarmScheduler.cancelReminder(
                        context = getApplication(),
                        module = AlarmScheduler.MODULE_EVENT,
                        itemId = item.id
                    )
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to delete event.") }
        }
    }

    fun addPayment(title: String, amount: Double, currency: String, paymentType: String, description: String, scheduledAtMillis: Long, alarmEnabled: Boolean) {
        if (title.isBlank()) {
            setError("Payment title is required.")
            return
        }
        if (amount <= 0.0) {
            setError("Payment amount must be greater than 0.")
            return
        }
        // Only allow alarms for future payments
        val now = System.currentTimeMillis()
        if (scheduledAtMillis <= now && alarmEnabled) {
            setError("Alarms can only be set for future payments.")
            return
        }

        viewModelScope.launch {
            paymentRepository.addPayment(
                title = title,
                amount = amount,
                currency = currency,
                paymentType = paymentType,
                description = description,
                scheduledAtMillis = scheduledAtMillis,
                alarmEnabled = alarmEnabled && scheduledAtMillis > now
            )
                .onSuccess { itemId ->
                    if (alarmEnabled && scheduledAtMillis > now) {
                        AlarmScheduler.scheduleReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_PAYMENT,
                            itemId = itemId,
                            title = title.trim(),
                            scheduledAtMillis = scheduledAtMillis
                        )
                    }
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to save payment.") }
        }
    }

    fun updatePayment(item: PaymentItem) {
        if (item.title.isBlank()) {
            setError("Payment title is required.")
            return
        }

        val now = System.currentTimeMillis()
        val isFuturePayment = item.scheduledAtMillis > now

        viewModelScope.launch {
            paymentRepository.updatePayment(item.copy(alarmEnabled = item.alarmEnabled && isFuturePayment))
                .onSuccess {
                    if (item.alarmEnabled && isFuturePayment) {
                        AlarmScheduler.scheduleReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_PAYMENT,
                            itemId = item.id,
                            title = item.title,
                            scheduledAtMillis = item.scheduledAtMillis
                        )
                    } else {
                        AlarmScheduler.cancelReminder(
                            context = getApplication(),
                            module = AlarmScheduler.MODULE_PAYMENT,
                            itemId = item.id
                        )
                    }
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to update payment.") }
        }
    }

    fun deletePayment(item: PaymentItem) {
        viewModelScope.launch {
            paymentRepository.deletePayment(item)
                .onSuccess {
                    AlarmScheduler.cancelReminder(
                        context = getApplication(),
                        module = AlarmScheduler.MODULE_PAYMENT,
                        itemId = item.id
                    )
                    clearError()
                }
                .onFailure { setError(it.message ?: "Unable to delete payment.") }
        }
    }

    private fun observeUserData(userId: String) {
        userDataJob?.cancel()
        userDataJob = viewModelScope.launch {
            launch {
                taskRepository.observeTasks(userId)
                    .catch { handleDataError(it, "tasks") }
                    .collect { tasks ->
                        _uiState.value = _uiState.value.copy(tasks = tasks)
                    }
            }
            launch {
                expenseRepository.observeExpenses(userId)
                    .catch { handleDataError(it, "expenses") }
                    .collect { expenses ->
                        _uiState.value = _uiState.value.copy(expenses = expenses)
                    }
            }
            launch {
                eventRepository.observeEvents(userId)
                    .catch { handleDataError(it, "events") }
                    .collect { events ->
                        _uiState.value = _uiState.value.copy(events = events)
                    }
            }
            launch {
                paymentRepository.observePayments(userId)
                    .catch { handleDataError(it, "payments") }
                    .collect { payments ->
                        _uiState.value = _uiState.value.copy(payments = payments)
                    }
            }
        }
    }

    private fun handleDataError(e: Throwable, type: String) {
        if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            // Ignore permission errors during potential logout/transition
            return
        }
        setError("Error loading $type: ${e.message}")
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            setError("Email and password are required.")
            return false
        }
        if (password.length < 6) {
            setError("Password must be at least 6 characters.")
            return false
        }
        return true
    }

    private fun setError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                application,
                authRepository = AuthRepositoryImpl(),
                taskRepository = TaskRepositoryImpl(),
                expenseRepository = ExpenseRepositoryImpl(),
                eventRepository = EventRepositoryImpl(),
                paymentRepository = PaymentRepositoryImpl()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
