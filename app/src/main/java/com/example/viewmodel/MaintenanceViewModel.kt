package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MaintenanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "essential_maintenance_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = MaintenanceRepository(db)

    // UI state states
    private val _currentRole = MutableStateFlow("Super Admin") // "Super Admin", "Entity Admin", "Technician", "End User"
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    private val _currentEntityId = MutableStateFlow(1L) // Default to Greenwood Society (1)
    val currentEntityId: StateFlow<Long> = _currentEntityId.asStateFlow()

    private val _selectedTab = MutableStateFlow("Home") // "Home", "New Request", "My Requests", "Notifications", "Profile"
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    // Database Flows
    val allEntities: StateFlow<List<EntityModel>> = repository.allEntities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServices: StateFlow<List<ServiceModel>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRequests: StateFlow<List<MaintenanceRequest>> = repository.allRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScheduledTasks: StateFlow<List<ScheduledTask>> = repository.allScheduledTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationModel>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered flows based on selected entity
    val activeEntity: StateFlow<EntityModel?> = combine(allEntities, _currentEntityId) { entities, id ->
        entities.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeServices: StateFlow<List<ServiceModel>> = combine(allServices, _currentEntityId) { services, id ->
        services.filter { it.entityId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRequests: StateFlow<List<MaintenanceRequest>> = combine(allRequests, _currentEntityId, _currentRole) { requests, id, role ->
        when (role) {
            "Super Admin" -> requests // Super Admin sees EVERYTHING
            "Technician" -> requests.filter { it.assignedTo.isNotEmpty() } // Tech sees all assigned requests
            else -> requests.filter { it.entityId == id } // Entity Admin / End User sees only their entity requests
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeScheduledTasks: StateFlow<List<ScheduledTask>> = combine(allScheduledTasks, _currentEntityId) { tasks, id ->
        tasks.filter { it.entityId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mock Technician names
    val techniciansList = listOf(
        "Technician Amit",
        "Plumber Rajesh",
        "HVAC Tech Vikram",
        "IT Tech Sunil",
        "Civil Lead Manoj",
        "Generator Tech Anil",
        "Gas Expert Rahul"
    )

    fun setRole(role: String) {
        _currentRole.value = role
    }

    fun setEntityId(id: Long) {
        _currentEntityId.value = id
    }

    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
    }

    // Raise new request
    fun raiseRequest(category: String, description: String, priority: String, location: String) {
        viewModelScope.launch {
            val req = MaintenanceRequest(
                entityId = _currentEntityId.value,
                serviceCategory = category,
                description = description,
                priority = priority,
                status = "Assigned", // Default state: starts assigned/open
                assignedTo = autoAssignTechnician(category),
                location = location,
                createdAt = System.currentTimeMillis()
            )
            repository.insertRequest(req)

            // Trigger notification
            val entityName = activeEntity.value?.name ?: "Property"
            repository.insertNotification(
                NotificationModel(
                    title = "New Request Raised",
                    message = "$entityName: $category ($priority) complaint filed at $location.",
                    type = "NewRequest",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private fun autoAssignTechnician(category: String): String {
        return when (category) {
            "Electricity", "Elevator", "Generator" -> "Technician Amit"
            "Water", "Plumbing" -> "Plumber Rajesh"
            "HVAC" -> "HVAC Tech Vikram"
            "Internet/Network" -> "IT Tech Sunil"
            "Road/Infra" -> "Civil Lead Manoj"
            "Gas" -> "Gas Expert Rahul"
            else -> "Technician Amit"
        }
    }

    // Update Status
    fun updateRequestStatus(requestId: Long, status: String) {
        viewModelScope.launch {
            repository.updateRequestStatus(requestId, status)

            // Create notification of update
            val req = allRequests.value.find { it.id == requestId }
            if (req != null) {
                repository.insertNotification(
                    NotificationModel(
                        title = "Request $status",
                        message = "${req.serviceCategory} request at ${req.location} is now $status.",
                        type = "StatusUpdate",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // Manual Assign Tech
    fun assignRequest(requestId: Long, technicianName: String) {
        viewModelScope.launch {
            repository.updateRequestAssignment(requestId, technicianName, "Assigned")

            val req = allRequests.value.find { it.id == requestId }
            if (req != null) {
                repository.insertNotification(
                    NotificationModel(
                        title = "Technician Assigned",
                        message = "$technicianName is assigned to resolve the ${req.serviceCategory} issue.",
                        type = "StatusUpdate",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // Feedback
    fun submitFeedback(requestId: Long, rating: Int, comment: String) {
        viewModelScope.launch {
            repository.updateRequestFeedback(requestId, rating, comment)
            repository.updateRequestStatus(requestId, "Verified")
        }
    }

    // Perform scheduled preventive maintenance
    fun performScheduledTask(taskId: Long) {
        viewModelScope.launch {
            repository.performScheduledTask(taskId)
        }
    }

    // Modify Service Status manually (Test Cascading Impacts!)
    fun changeServiceStatus(category: String, status: String) {
        viewModelScope.launch {
            repository.updateServiceStatusAndCascading(_currentEntityId.value, category, status)
        }
    }

    // Notification Controls
    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    // Helper: Reset database to Healthy for the active entity
    fun resetActiveEntityServices() {
        viewModelScope.launch {
            val entityId = _currentEntityId.value
            val services = allServices.value.filter { it.entityId == entityId }
            services.forEach { service ->
                repository.updateServiceStatusAndCascading(entityId, service.category, "Healthy")
            }
            repository.insertNotification(
                NotificationModel(
                    title = "System Reset",
                    message = "All interconnected utilities have been restored to Healthy status.",
                    type = "StatusUpdate",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
