package com.faheem.pocketapp.data.repository

import com.faheem.pocketapp.PaymentItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface PaymentRepository {
    suspend fun addPayment(
        title: String,
        amount: Double,
        paymentType: String, // "have_to_take" or "have_to_give"
        description: String,
        scheduledAtMillis: Long,
        alarmEnabled: Boolean
    ): Result<Unit>

    suspend fun updatePayment(item: PaymentItem): Result<Unit>
    suspend fun deletePayment(item: PaymentItem): Result<Unit>
    fun observePayments(userId: String): Flow<List<PaymentItem>>
}

class PaymentRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : PaymentRepository {

    override suspend fun addPayment(
        title: String,
        amount: Double,
        paymentType: String,
        description: String,
        scheduledAtMillis: Long,
        alarmEnabled: Boolean
    ): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            val document = paymentsCollection(user.uid).document()
            val now = System.currentTimeMillis()
            val payload = mapOf(
                "title" to title.trim(),
                "amount" to amount,
                "paymentType" to paymentType,
                "description" to description.trim(),
                "scheduledAtMillis" to scheduledAtMillis,
                "alarmEnabled" to alarmEnabled,
                "updatedAt" to now,
                "createdAt" to now
            )
            document.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePayment(item: PaymentItem): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            val payload = mapOf(
                "title" to item.title.trim(),
                "amount" to item.amount,
                "paymentType" to item.paymentType,
                "description" to item.description.trim(),
                "scheduledAtMillis" to item.scheduledAtMillis,
                "alarmEnabled" to item.alarmEnabled,
                "updatedAt" to System.currentTimeMillis()
            )
            paymentsCollection(user.uid).document(item.id).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePayment(item: PaymentItem): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            paymentsCollection(user.uid).document(item.id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observePayments(userId: String): Flow<List<PaymentItem>> {
        return callbackFlow {
            val now = System.currentTimeMillis()
            val listener = paymentsCollection(userId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val payments = snapshot?.documents
                    ?.map { doc ->
                        PaymentItem(
                            id = doc.id,
                            title = doc.getString("title").orEmpty(),
                            amount = doc.getDouble("amount") ?: 0.0,
                            paymentType = doc.getString("paymentType").orEmpty(),
                            description = doc.getString("description").orEmpty(),
                            scheduledAtMillis = doc.getLong("scheduledAtMillis") ?: 0L,
                            alarmEnabled = doc.getBoolean("alarmEnabled") ?: false,
                            updatedAt = doc.getLong("updatedAt") ?: 0L,
                            isFuturePayment = (doc.getLong("scheduledAtMillis") ?: 0L) > now
                        )
                    }
                    ?.filter { it.scheduledAtMillis > now } // Only show future payments
                    ?.sortedByDescending { it.scheduledAtMillis }
                    ?: emptyList()
                trySend(payments)
            }
            awaitClose { listener.remove() }
        }
    }

    private fun paymentsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("payments")
}

