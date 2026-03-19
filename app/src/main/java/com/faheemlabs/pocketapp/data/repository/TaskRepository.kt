package com.faheemlabs.pocketapp.data.repository

import com.faheemlabs.pocketapp.TaskItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

interface TaskRepository {
    suspend fun addTask(
        title: String,
        details: String,
        attachmentUrl: String,
        scheduledAtMillis: Long,
        alarmEnabled: Boolean,
        recurrencePattern: String,
        priority: String
    ): Result<String>
    suspend fun updateTask(item: TaskItem): Result<Unit>
    suspend fun deleteTask(item: TaskItem): Result<Unit>
    fun observeTasks(userId: String): kotlinx.coroutines.flow.Flow<List<TaskItem>>
}

class TaskRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : TaskRepository {

    override suspend fun addTask(
        title: String,
        details: String,
        attachmentUrl: String,
        scheduledAtMillis: Long,
        alarmEnabled: Boolean,
        recurrencePattern: String,
        priority: String
    ): Result<String> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            val document = tasksCollection(user.uid).document()
            val now = System.currentTimeMillis()
            val payload = mapOf(
                "title" to title.trim(),
                "details" to details.trim(),
                "attachmentUrl" to attachmentUrl.trim(),
                "scheduledAtMillis" to scheduledAtMillis,
                "alarmEnabled" to alarmEnabled,
                "recurrencePattern" to recurrencePattern,
                "priority" to priority,
                "updatedAt" to now
            )
            document.set(payload).await()
            Result.success(document.id)
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
                "attachmentUrl" to item.attachmentUrl.trim(),
                "scheduledAtMillis" to item.scheduledAtMillis,
                "alarmEnabled" to item.alarmEnabled,
                "recurrencePattern" to item.recurrencePattern,
                "priority" to item.priority,
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
                            attachmentUrl = doc.getString("attachmentUrl").orEmpty(),
                            scheduledAtMillis = doc.getLong("scheduledAtMillis") ?: 0L,
                            alarmEnabled = doc.getBoolean("alarmEnabled") ?: false,
                            recurrencePattern = doc.getString("recurrencePattern") ?: "none",
                            priority = doc.getString("priority") ?: "medium",
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

