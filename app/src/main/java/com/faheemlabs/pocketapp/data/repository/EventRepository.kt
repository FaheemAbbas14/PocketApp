package com.faheemlabs.pocketapp.data.repository

import com.faheemlabs.pocketapp.EventItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await

interface EventRepository {
    suspend fun addEvent(
        title: String,
        description: String,
        attachmentUrl: String,
        eventDateMillis: Long,
        locationName: String,
        latitude: Double?,
        longitude: Double?,
        alarmEnabled: Boolean,
        recurrencePattern: String
    ): Result<String>

    suspend fun updateEvent(item: EventItem): Result<Unit>
    suspend fun deleteEvent(item: EventItem): Result<Unit>
    fun observeEvents(userId: String): kotlinx.coroutines.flow.Flow<List<EventItem>>
}

class EventRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : EventRepository {

    override suspend fun addEvent(
        title: String,
        description: String,
        attachmentUrl: String,
        eventDateMillis: Long,
        locationName: String,
        latitude: Double?,
        longitude: Double?,
        alarmEnabled: Boolean,
        recurrencePattern: String
    ): Result<String> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            val document = eventsCollection(user.uid).document()
            val now = System.currentTimeMillis()
            val payload = mapOf(
                "title" to title.trim(),
                "description" to description.trim(),
                "attachmentUrl" to attachmentUrl.trim(),
                "eventDateMillis" to eventDateMillis,
                "locationName" to locationName.trim(),
                "latitude" to latitude,
                "longitude" to longitude,
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

    override suspend fun updateEvent(item: EventItem): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            val payload = mapOf(
                "title" to item.title.trim(),
                "description" to item.description.trim(),
                "attachmentUrl" to item.attachmentUrl.trim(),
                "eventDateMillis" to item.eventDateMillis,
                "locationName" to item.locationName.trim(),
                "latitude" to item.latitude,
                "longitude" to item.longitude,
                "alarmEnabled" to item.alarmEnabled,
                "recurrencePattern" to item.recurrencePattern,
                "updatedAt" to System.currentTimeMillis()
            )
            eventsCollection(user.uid).document(item.id).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEvent(item: EventItem): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("User not logged in")
            eventsCollection(user.uid).document(item.id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeEvents(userId: String): kotlinx.coroutines.flow.Flow<List<EventItem>> {
        return kotlinx.coroutines.flow.callbackFlow {
            val listener = eventsCollection(userId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents
                    ?.map { doc ->
                        EventItem(
                            id = doc.id,
                            title = doc.getString("title").orEmpty(),
                            description = doc.getString("description").orEmpty(),
                            attachmentUrl = doc.getString("attachmentUrl").orEmpty(),
                            eventDateMillis = doc.getLong("eventDateMillis") ?: 0L,
                            locationName = doc.getString("locationName").orEmpty(),
                            latitude = doc.getDouble("latitude"),
                            longitude = doc.getDouble("longitude"),
                            alarmEnabled = doc.getBoolean("alarmEnabled") ?: false,
                            recurrencePattern = doc.getString("recurrencePattern") ?: "none",
                            updatedAt = doc.getLong("updatedAt") ?: 0L
                        )
                    }
                    ?.sortedByDescending { it.eventDateMillis }
                    ?: emptyList()
                trySend(events)
            }
            awaitClose { listener.remove() }
        }
    }

    private fun eventsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("events")
}
