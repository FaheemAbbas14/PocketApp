package com.faheemlabs.pocketapp.data.repository

import com.faheemlabs.pocketapp.ExpenseItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

interface ExpenseRepository {
    suspend fun addExpense(
        title: String,
        amount: Double,
        currency: String,
        category: String,
        paymentMethod: String,
        notes: String,
        attachmentUrl: String,
        scheduledAtMillis: Long,
        alarmEnabled: Boolean,
        recurrencePattern: String
    ): Result<String>
    suspend fun updateExpense(item: ExpenseItem): Result<Unit>
    suspend fun deleteExpense(item: ExpenseItem): Result<Unit>
    fun observeExpenses(userId: String): kotlinx.coroutines.flow.Flow<List<ExpenseItem>>
    suspend fun setMonthlyBudget(limitAmount: Double, currency: String): Result<Unit>
    suspend fun clearMonthlyBudget(): Result<Unit>
    fun observeMonthlyBudget(userId: String): kotlinx.coroutines.flow.Flow<ExpenseBudget?>
}

data class ExpenseBudget(
    val limitAmount: Double,
    val currency: String,
    val updatedAt: Long
)

class ExpenseRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ExpenseRepository {

    private val monthlyBudgetDocId = "monthlyExpenseBudget"

    override suspend fun addExpense(
        title: String,
        amount: Double,
        currency: String,
        category: String,
        paymentMethod: String,
        notes: String,
        attachmentUrl: String,
        scheduledAtMillis: Long,
        alarmEnabled: Boolean,
        recurrencePattern: String
    ): Result<String> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            val document = expensesCollection(user.uid).document()
            val now = System.currentTimeMillis()
            val payload = mapOf(
                "title" to title.trim(),
                "amount" to amount,
                "currency" to currency,
                "category" to category.trim(),
                "paymentMethod" to paymentMethod.trim(),
                "notes" to notes.trim(),
                "attachmentUrl" to attachmentUrl.trim(),
                "scheduledAtMillis" to scheduledAtMillis,
                "alarmEnabled" to alarmEnabled,
                "recurrencePattern" to recurrencePattern,
                "updatedAt" to now
            )
            document.set(payload).await()
            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExpense(item: ExpenseItem): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            val payload = mapOf(
                "title" to item.title.trim(),
                "amount" to item.amount,
                "currency" to item.currency,
                "category" to item.category.trim(),
                "paymentMethod" to item.paymentMethod.trim(),
                "notes" to item.notes.trim(),
                "attachmentUrl" to item.attachmentUrl.trim(),
                "scheduledAtMillis" to item.scheduledAtMillis,
                "alarmEnabled" to item.alarmEnabled,
                "recurrencePattern" to item.recurrencePattern,
                "updatedAt" to System.currentTimeMillis()
            )
            expensesCollection(user.uid).document(item.id).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(item: ExpenseItem): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            expensesCollection(user.uid).document(item.id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeExpenses(userId: String): kotlinx.coroutines.flow.Flow<List<ExpenseItem>> {
        return kotlinx.coroutines.flow.callbackFlow {
            val listener = expensesCollection(userId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents
                    ?.map { doc ->
                        ExpenseItem(
                            id = doc.id,
                            title = doc.getString("title").orEmpty(),
                            amount = doc.getDouble("amount") ?: 0.0,
                            currency = doc.getString("currency") ?: "USD",
                            category = doc.getString("category").orEmpty(),
                            paymentMethod = doc.getString("paymentMethod").orEmpty(),
                            notes = doc.getString("notes").orEmpty(),
                            attachmentUrl = doc.getString("attachmentUrl").orEmpty(),
                            scheduledAtMillis = doc.getLong("scheduledAtMillis") ?: 0L,
                            alarmEnabled = doc.getBoolean("alarmEnabled") ?: false,
                            recurrencePattern = doc.getString("recurrencePattern") ?: "none",
                            updatedAt = doc.getLong("updatedAt") ?: 0L
                        )
                    }
                    ?.sortedByDescending { it.scheduledAtMillis }
                    ?: emptyList()
                trySend(expenses)
            }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun setMonthlyBudget(limitAmount: Double, currency: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            val payload = mapOf(
                "limitAmount" to limitAmount,
                "currency" to currency.ifBlank { "USD" },
                "updatedAt" to System.currentTimeMillis()
            )
            settingsCollection(user.uid).document(monthlyBudgetDocId).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearMonthlyBudget(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            settingsCollection(user.uid).document(monthlyBudgetDocId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeMonthlyBudget(userId: String): kotlinx.coroutines.flow.Flow<ExpenseBudget?> {
        return kotlinx.coroutines.flow.callbackFlow {
            val listener = settingsCollection(userId)
                .document(monthlyBudgetDocId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        trySend(null)
                        return@addSnapshotListener
                    }

                    val budget = ExpenseBudget(
                        limitAmount = snapshot.getDouble("limitAmount") ?: 0.0,
                        currency = snapshot.getString("currency") ?: "USD",
                        updatedAt = snapshot.getLong("updatedAt") ?: 0L
                    )
                    trySend(budget)
                }
            awaitClose { listener.remove() }
        }
    }

    private fun expensesCollection(uid: String) =
        firestore.collection("users").document(uid).collection("expenses")

    private fun settingsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("settings")
}
