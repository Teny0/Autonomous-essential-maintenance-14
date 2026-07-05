package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EntityDao {
    @Query("SELECT * FROM entities")
    fun getAllEntities(): Flow<List<EntityModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(entity: EntityModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntities(entities: List<EntityModel>)
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services")
    fun getAllServices(): Flow<List<ServiceModel>>

    @Query("SELECT * FROM services WHERE entityId = :entityId")
    fun getServicesForEntity(entityId: Long): Flow<List<ServiceModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceModel>)

    @Query("UPDATE services SET status = :status WHERE id = :serviceId")
    suspend fun updateServiceStatus(serviceId: Long, status: String)
}

@Dao
interface RequestDao {
    @Query("SELECT * FROM requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<MaintenanceRequest>>

    @Query("SELECT * FROM requests WHERE entityId = :entityId ORDER BY createdAt DESC")
    fun getRequestsForEntity(entityId: Long): Flow<List<MaintenanceRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: MaintenanceRequest): Long

    @Query("UPDATE requests SET status = :status WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: Long, status: String)

    @Query("UPDATE requests SET status = :status, resolvedAt = :resolvedAt WHERE id = :requestId")
    suspend fun resolveRequest(requestId: Long, status: String, resolvedAt: Long)

    @Query("UPDATE requests SET assignedTo = :assignedTo, status = :status WHERE id = :requestId")
    suspend fun updateRequestAssignment(requestId: Long, assignedTo: String, status: String)

    @Query("UPDATE requests SET feedbackRating = :rating, feedbackComment = :comment WHERE id = :requestId")
    suspend fun updateRequestFeedback(requestId: Long, rating: Int, comment: String)

    @Delete
    suspend fun deleteRequest(request: MaintenanceRequest)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM scheduled_tasks ORDER BY nextDueDate ASC")
    fun getAllScheduledTasks(): Flow<List<ScheduledTask>>

    @Query("SELECT * FROM scheduled_tasks WHERE entityId = :entityId ORDER BY nextDueDate ASC")
    fun getScheduledTasksForEntity(entityId: Long): Flow<List<ScheduledTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledTask(task: ScheduledTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledTasks(tasks: List<ScheduledTask>)

    @Query("UPDATE scheduled_tasks SET lastPerformedAt = :timestamp, nextDueDate = :nextDueDate WHERE id = :taskId")
    suspend fun updateScheduledTaskLastPerformed(taskId: Long, timestamp: Long, nextDueDate: Long)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationModel): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}
