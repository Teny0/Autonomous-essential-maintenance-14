package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entities")
data class EntityModel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "House", "Company", "CityZone"
    val address: String
)

@Entity(tableName = "services")
data class ServiceModel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityId: Long,
    val category: String, // "Electricity", "Water", "Plumbing", "Gas", "Internet/Network", "Elevator", "Generator", "HVAC", "Waste", "Security", "Road/Infra"
    val status: String, // "Healthy", "Maintenance", "Down"
    val dependencies: String // Comma-separated list of service categories that this service depends on
)

@Entity(tableName = "requests")
data class MaintenanceRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityId: Long,
    val serviceCategory: String,
    val description: String,
    val priority: String, // "Low", "Medium", "High", "Emergency"
    val status: String, // "Assigned", "In Progress", "Resolved", "Verified"
    val assignedTo: String,
    val location: String, // unit/floor/zone
    val createdAt: Long,
    val resolvedAt: Long = 0L,
    val feedbackRating: Int = 0, // 1 to 5, or 0 if not rated yet
    val feedbackComment: String = ""
)

@Entity(tableName = "scheduled_tasks")
data class ScheduledTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityId: Long,
    val serviceCategory: String,
    val description: String,
    val frequency: String, // "Weekly", "Monthly", "Quarterly"
    val lastPerformedAt: Long = 0L,
    val nextDueDate: Long,
    val assignedTo: String
)

@Entity(tableName = "notifications")
data class NotificationModel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "CascadingAlert", "StatusUpdate", "NewRequest", "ScheduledReminder"
    val timestamp: Long,
    val isRead: Boolean = false
)
