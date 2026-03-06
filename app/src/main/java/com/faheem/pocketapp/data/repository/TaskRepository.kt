package com.faheem.pocketapp.data.repository

import com.faheem.pocketapp.TaskItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

interface TaskRepository {
    suspend fun addTask(title: String, details: String, scheduledAtMillis: Long, alarmEnabled: Boolean): Result<Unit>
    suspend fun updateTask(item: TaskItem): Result<Unit>
    suspend fun deleteTask(item: TaskItem): Result<Unit>
    fun observeTasks(userId: String): kotlinx.coroutines.flow.Flow<List<TaskItem>>
}

class TaskRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : TaskRepository {

    override suspend fun addTask(title: String, details: String, scheduledAtMillis: Long, alarmEnabled: Boolean): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTask(item: TaskItem): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            val payload = mapOf(
                "title" to item.title.trim(),
                "details" to item.details.trim(),
                "scheduledAtMillis" to item.scheduledAtMillis,
                "alarmEnabled" to item.alarmEnabled,
                "updatedAt" to System.currentTimeMillis()
            )
            tasksCollection(user.uid).document(item.id).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(item: TaskItem): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            tasksCollection(user.uid).document(item.id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeTasks(userId: String): kotlinx.coroutines.flow.Flow<List<TaskItem>> {
        return kotlinx.coroutines.flow.callbackFlow {
            val listener = tasksCollection(userId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
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
                trySend(tasks)
            }
            awaitClose { listener.remove() }
        }
    }

    private fun tasksCollection(uid: String) =
        firestore.collection("users").document(uid).collection("tasks")
}

