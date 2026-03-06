package com.faheem.pocketapp.data.repository

import com.faheem.pocketapp.ExpenseItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface ExpenseRepository {
    suspend fun addExpense(
        title: String,
        amount: Double,
        category: String,
        paymentMethod: String,
        notes: String,
        scheduledAtMillis: Long,
        alarmEnabled: Boolean
    ): Result<Unit>
    suspend fun updateExpense(item: ExpenseItem): Result<Unit>
    suspend fun deleteExpense(item: ExpenseItem): Result<Unit>
    fun observeExpenses(userId: String): kotlinx.coroutines.flow.Flow<List<ExpenseItem>>
}

class ExpenseRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ExpenseRepository {

    override suspend fun addExpense(
        title: String,
        amount: Double,
        category: String,
        paymentMethod: String,
        notes: String,
        scheduledAtMillis: Long,
        alarmEnabled: Boolean
    ): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
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
            Result.success(Unit)
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
                "category" to item.category.trim(),
                "paymentMethod" to item.paymentMethod.trim(),
                "notes" to item.notes.trim(),
                "scheduledAtMillis" to item.scheduledAtMillis,
                "alarmEnabled" to item.alarmEnabled,
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
                trySend(expenses)
            }
            awaitClose { listener.remove() }
        }
    }

    private fun expensesCollection(uid: String) =
        firestore.collection("users").document(uid).collection("expenses")
}

