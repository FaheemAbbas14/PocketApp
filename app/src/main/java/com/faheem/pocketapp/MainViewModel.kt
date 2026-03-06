package com.faheem.pocketapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    val alarmEnabled: Boolean,
    val updatedAt: Long
)

data class PocketUiState(
    val isLoading: Boolean = false,
    val currentUserEmail: String? = null,
    val tasks: List<TaskItem> = emptyList(),
    val expenses: List<ExpenseItem> = emptyList(),
    val events: List<EventItem> = emptyList(),
    val errorMessage: String? = null
)

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(PocketUiState())
    val uiState: StateFlow<PocketUiState> = _uiState.asStateFlow()

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var tasksListener: ListenerRegistration? = null
    private var expensesListener: ListenerRegistration? = null
    private var eventsListener: ListenerRegistration? = null

    init {
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            handleUserChanged(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(authListener!!)
        handleUserChanged(auth.currentUser)
    }

    fun login(email: String, password: String) {
        if (!validateCredentials(email, password)) return

        executeAuthAction {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
        }
    }

    fun register(email: String, password: String) {
        if (!validateCredentials(email, password)) return

        executeAuthAction {
            auth.createUserWithEmailAndPassword(email.trim(), password).await()
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun addTask(title: String, details: String, scheduledAtMillis: Long, alarmEnabled: Boolean) {
        val user = auth.currentUser ?: run {
            setError("Please log in first.")
            return
        }
        if (title.isBlank()) {
            setError("Task title is required.")
            return
        }

        viewModelScope.launch {
            try {
                val document = tasksCollection(user.uid).document()
                val now = System.currentTimeMillis()
                val payload = mapOf(
                    "title" to title.trim(),
                    "details" to details.trim(),
                    "scheduledAtMillis" to scheduledAtMillis,
                    "alarmEnabled" to alarmEnabled,
                    "updatedAt" to now
                )
                document.set(payload).await()
                if (alarmEnabled) {
                    AlarmScheduler.scheduleReminder(
                        context = getApplication(),
                        module = AlarmScheduler.MODULE_TASK,
                        itemId = document.id,
                        title = title.trim(),
                        scheduledAtMillis = scheduledAtMillis
                    )
                }
                clearError()
            } catch (e: Exception) {
                setError(e.message ?: "Unable to save task.")
            }
        }
    }

    fun updateTask(item: TaskItem) {
        val user = auth.currentUser ?: return
        if (item.title.isBlank()) {
            setError("Task title is required.")
            return
        }

        viewModelScope.launch {
            try {
                val payload = mapOf(
                    "title" to item.title.trim(),
                    "details" to item.details.trim(),
                    "scheduledAtMillis" to item.scheduledAtMillis,
                    "alarmEnabled" to item.alarmEnabled,
                    "updatedAt" to System.currentTimeMillis()
                )
                tasksCollection(user.uid).document(item.id).set(payload).await()

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
            } catch (e: Exception) {
                setError(e.message ?: "Unable to update task.")
            }
        }
    }

    fun deleteTask(item: TaskItem) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                tasksCollection(user.uid).document(item.id).delete().await()
                AlarmScheduler.cancelReminder(
                    context = getApplication(),
                    module = AlarmScheduler.MODULE_TASK,
                    itemId = item.id
                )
                clearError()
            } catch (e: Exception) {
                setError(e.message ?: "Unable to delete task.")
            }
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        paymentMethod: String,
        notes: String,
        scheduledAtMillis: Long,
        alarmEnabled: Boolean
    ) {
        val user = auth.currentUser ?: run {
            setError("Please log in first.")
            return
        }
        if (title.isBlank()) {
            setError("Expense title is required.")
            return
        }
        if (amount <= 0.0) {
            setError("Expense amount must be greater than 0.")
            return
        }

        viewModelScope.launch {
            try {
                val document = expensesCollection(user.uid).document()
                val now = System.currentTimeMillis()
                val payload = mapOf(
                    "title" to title.trim(),
                    "amount" to amount,
                    "category" to category.trim(),
                    "paymentMethod" to paymentMethod.trim(),
                    "notes" to notes.trim(),
                    "scheduledAtMillis" to scheduledAtMillis,
                    "alarmEnabled" to alarmEnabled,
                    "updatedAt" to now
                )
                document.set(payload).await()
                if (alarmEnabled) {
                    AlarmScheduler.scheduleReminder(
                        context = getApplication(),
                        module = AlarmScheduler.MODULE_EXPENSE,
                        itemId = document.id,
                        title = title.trim(),
                        scheduledAtMillis = scheduledAtMillis
                    )
                }
                clearError()
            } catch (e: Exception) {
                setError(e.message ?: "Unable to save expense.")
            }
        }
    }

    fun updateExpense(item: ExpenseItem) {
        val user = auth.currentUser ?: return
        if (item.title.isBlank()) {
            setError("Expense title is required.")
            return
        }
        if (item.amount <= 0.0) {
            setError("Expense amount must be greater than 0.")
            return
        }

        viewModelScope.launch {
            try {
                val payload = mapOf(
                    "title" to item.title.trim(),
                    "amount" to item.amount,
                    "category" to item.category.trim(),
                    "paymentMethod" to item.paymentMethod.trim(),
                    "notes" to item.notes.trim(),
                    "scheduledAtMillis" to item.scheduledAtMillis,
                    "alarmEnabled" to item.alarmEnabled,
                    "updatedAt" to System.currentTimeMillis()
                )
                expensesCollection(user.uid).document(item.id).set(payload).await()

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
            } catch (e: Exception) {
                setError(e.message ?: "Unable to update expense.")
            }
        }
    }

    fun deleteExpense(item: ExpenseItem) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                expensesCollection(user.uid).document(item.id).delete().await()
                AlarmScheduler.cancelReminder(
                    context = getApplication(),
                    module = AlarmScheduler.MODULE_EXPENSE,
                    itemId = item.id
                )
                clearError()
            } catch (e: Exception) {
                setError(e.message ?: "Unable to delete expense.")
            }
        }
    }

    fun addEvent(title: String, description: String, eventDateMillis: Long, alarmEnabled: Boolean) {
        val user = auth.currentUser ?: run {
            setError("Please log in first.")
            return
        }
        if (title.isBlank()) {
            setError("Event title is required.")
            return
        }

        viewModelScope.launch {
            try {
                val document = eventsCollection(user.uid).document()
                val now = System.currentTimeMillis()
                val payload = mapOf(
                    "title" to title.trim(),
                    "description" to description.trim(),
                    "eventDateMillis" to eventDateMillis,
                    "alarmEnabled" to alarmEnabled,
                    "updatedAt" to now
                )
                document.set(payload).await()
                if (alarmEnabled) {
                    AlarmScheduler.scheduleReminder(
                        context = getApplication(),
                        module = AlarmScheduler.MODULE_EVENT,
                        itemId = document.id,
                        title = title.trim(),
                        scheduledAtMillis = eventDateMillis
                    )
                }
                clearError()
            } catch (e: Exception) {
                setError(e.message ?: "Unable to save event.")
            }
        }
    }

    fun updateEvent(item: EventItem) {
        val user = auth.currentUser ?: return
        if (item.title.isBlank()) {
            setError("Event title is required.")
            return
        }

        viewModelScope.launch {
            try {
                val payload = mapOf(
                    "title" to item.title.trim(),
                    "description" to item.description.trim(),
                    "eventDateMillis" to item.eventDateMillis,
                    "alarmEnabled" to item.alarmEnabled,
                    "updatedAt" to System.currentTimeMillis()
                )
                eventsCollection(user.uid).document(item.id).set(payload).await()

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
            } catch (e: Exception) {
                setError(e.message ?: "Unable to update event.")
            }
        }
    }

    fun deleteEvent(item: EventItem) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                eventsCollection(user.uid).document(item.id).delete().await()
                AlarmScheduler.cancelReminder(
                    context = getApplication(),
                    module = AlarmScheduler.MODULE_EVENT,
                    itemId = item.id
                )
                clearError()
            } catch (e: Exception) {
                setError(e.message ?: "Unable to delete event.")
            }
        }
    }

    private fun executeAuthAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                action()
                clearError()
            } catch (e: Exception) {
                setError(e.message ?: "Authentication failed.")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun handleUserChanged(user: FirebaseUser?) {
        tasksListener?.remove()
        expensesListener?.remove()
        eventsListener?.remove()
        tasksListener = null
        expensesListener = null
        eventsListener = null

        if (user == null) {
            _uiState.value = PocketUiState()
            return
        }

        _uiState.value = _uiState.value.copy(
            currentUserEmail = user.email,
            isLoading = true,
            errorMessage = null
        )

        tasksListener = tasksCollection(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    setError(error.message ?: "Unable to sync tasks.")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents
                    ?.map { doc ->
                        TaskItem(
                            id = doc.id,
                            title = doc.getString("title").orEmpty(),
                            details = doc.getString("details").orEmpty(),
                            scheduledAtMillis = doc.getLong("scheduledAtMillis") ?: 0L,
                            alarmEnabled = doc.getBoolean("alarmEnabled") ?: false,
                            updatedAt = doc.getLong("updatedAt") ?: 0L
                        )
                    }
                    ?.sortedByDescending { it.scheduledAtMillis }
                    ?: emptyList()

                _uiState.value = _uiState.value.copy(tasks = tasks, isLoading = false)
            }

        expensesListener = expensesCollection(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    setError(error.message ?: "Unable to sync expenses.")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@addSnapshotListener
                }

                val expenses = snapshot?.documents
                    ?.map { doc ->
                        ExpenseItem(
                            id = doc.id,
                            title = doc.getString("title").orEmpty(),
                            amount = doc.getDouble("amount") ?: 0.0,
                            category = doc.getString("category").orEmpty(),
                            paymentMethod = doc.getString("paymentMethod").orEmpty(),
                            notes = doc.getString("notes").orEmpty(),
                            scheduledAtMillis = doc.getLong("scheduledAtMillis") ?: 0L,
                            alarmEnabled = doc.getBoolean("alarmEnabled") ?: false,
                            updatedAt = doc.getLong("updatedAt") ?: 0L
                        )
                    }
                    ?.sortedByDescending { it.scheduledAtMillis }
                    ?: emptyList()

                _uiState.value = _uiState.value.copy(expenses = expenses, isLoading = false)
            }

        eventsListener = eventsCollection(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    setError(error.message ?: "Unable to sync events.")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@addSnapshotListener
                }

                val events = snapshot?.documents
                    ?.map { doc ->
                        EventItem(
                            id = doc.id,
                            title = doc.getString("title").orEmpty(),
                            description = doc.getString("description").orEmpty(),
                            eventDateMillis = doc.getLong("eventDateMillis") ?: 0L,
                            alarmEnabled = doc.getBoolean("alarmEnabled") ?: false,
                            updatedAt = doc.getLong("updatedAt") ?: 0L
                        )
                    }
                    ?.sortedByDescending { it.eventDateMillis }
                    ?: emptyList()

                _uiState.value = _uiState.value.copy(events = events, isLoading = false)
            }
    }

    private fun tasksCollection(uid: String) =
        firestore.collection("users").document(uid).collection("tasks")

    private fun expensesCollection(uid: String) =
        firestore.collection("users").document(uid).collection("expenses")

    private fun eventsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("events")

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

    override fun onCleared() {
        authListener?.let(auth::removeAuthStateListener)
        tasksListener?.remove()
        expensesListener?.remove()
        eventsListener?.remove()
        super.onCleared()
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
