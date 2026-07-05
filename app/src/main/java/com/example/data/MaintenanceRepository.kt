package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MaintenanceRepository(private val db: AppDatabase) {

    private val entityDao = db.entityDao()
    private val serviceDao = db.serviceDao()
    private val requestDao = db.requestDao()
    private val scheduleDao = db.scheduleDao()
    private val notificationDao = db.notificationDao()

    // Flow queries
    val allEntities: Flow<List<EntityModel>> = entityDao.getAllEntities()
    val allServices: Flow<List<ServiceModel>> = serviceDao.getAllServices()
    val allRequests: Flow<List<MaintenanceRequest>> = requestDao.getAllRequests()
    val allScheduledTasks: Flow<List<ScheduledTask>> = scheduleDao.getAllScheduledTasks()
    val allNotifications: Flow<List<NotificationModel>> = notificationDao.getAllNotifications()

    fun getServicesForEntity(entityId: Long): Flow<List<ServiceModel>> =
        serviceDao.getServicesForEntity(entityId)

    fun getRequestsForEntity(entityId: Long): Flow<List<MaintenanceRequest>> =
        requestDao.getRequestsForEntity(entityId)

    fun getScheduledTasksForEntity(entityId: Long): Flow<List<ScheduledTask>> =
        scheduleDao.getScheduledTasksForEntity(entityId)

    init {
        // Run seed in a background scope
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        val currentEntities = entityDao.getAllEntities().first()
        if (currentEntities.isEmpty()) {
            // Seed Entities
            val ent1Id = entityDao.insertEntity(
                EntityModel(name = "Greenwood Residential Society", type = "House", address = "Sector 15, Green Avenue")
            )
            val ent2Id = entityDao.insertEntity(
                EntityModel(name = "Apex Corporate Software Park", type = "Company", address = "Building 4B, Tech Zone")
            )
            val ent3Id = entityDao.insertEntity(
                EntityModel(name = "Metro Zone 4 - Downtown", type = "CityZone", address = "Central Municipal District")
            )

            // Seed Services for Greenwood Society (entityId = 1)
            serviceDao.insertServices(
                listOf(
                    ServiceModel(entityId = ent1Id, category = "Electricity", status = "Healthy", dependencies = ""),
                    ServiceModel(entityId = ent1Id, category = "Water", status = "Healthy", dependencies = "Electricity,Plumbing"),
                    ServiceModel(entityId = ent1Id, category = "Plumbing", status = "Healthy", dependencies = ""),
                    ServiceModel(entityId = ent1Id, category = "Security", status = "Healthy", dependencies = "Electricity"),
                    ServiceModel(entityId = ent1Id, category = "Elevator", status = "Healthy", dependencies = "Electricity"),
                    ServiceModel(entityId = ent1Id, category = "Internet/Network", status = "Healthy", dependencies = "Electricity"),
                    ServiceModel(entityId = ent1Id, category = "Waste", status = "Healthy", dependencies = "")
                )
            )

            // Seed Services for Apex Corporate Park (entityId = 2)
            serviceDao.insertServices(
                listOf(
                    ServiceModel(entityId = ent2Id, category = "Electricity", status = "Healthy", dependencies = ""),
                    ServiceModel(entityId = ent2Id, category = "Generator", status = "Healthy", dependencies = "Electricity"),
                    ServiceModel(entityId = ent2Id, category = "HVAC", status = "Healthy", dependencies = "Electricity,Generator"),
                    ServiceModel(entityId = ent2Id, category = "Internet/Network", status = "Healthy", dependencies = "Electricity"),
                    ServiceModel(entityId = ent2Id, category = "Elevator", status = "Healthy", dependencies = "Electricity"),
                    ServiceModel(entityId = ent2Id, category = "Security", status = "Healthy", dependencies = "Electricity,Internet/Network"),
                    ServiceModel(entityId = ent2Id, category = "Waste", status = "Healthy", dependencies = "")
                )
            )

            // Seed Services for Metro Zone 4 (entityId = 3)
            serviceDao.insertServices(
                listOf(
                    ServiceModel(entityId = ent3Id, category = "Electricity", status = "Healthy", dependencies = ""),
                    ServiceModel(entityId = ent3Id, category = "Water", status = "Healthy", dependencies = "Electricity"),
                    ServiceModel(entityId = ent3Id, category = "Gas", status = "Healthy", dependencies = ""),
                    ServiceModel(entityId = ent3Id, category = "Road/Infra", status = "Healthy", dependencies = ""),
                    ServiceModel(entityId = ent3Id, category = "Waste", status = "Healthy", dependencies = "")
                )
            )

            // Seed Requests
            val now = System.currentTimeMillis()
            requestDao.insertRequest(
                MaintenanceRequest(
                    entityId = ent1Id,
                    serviceCategory = "Elevator",
                    description = "Elevator B making grinding noises and moving slowly. Occurs on 4th floor.",
                    priority = "High",
                    status = "Assigned",
                    assignedTo = "Technician Amit",
                    location = "Tower 2, Floor 4",
                    createdAt = now - (3600000 * 5) // 5 hours ago
                )
            )
            requestDao.insertRequest(
                MaintenanceRequest(
                    entityId = ent1Id,
                    serviceCategory = "Water",
                    description = "Water supply leakage reported near the water pump room in the basement.",
                    priority = "Emergency",
                    status = "In Progress",
                    assignedTo = "Plumber Rajesh",
                    location = "Basement, Block A",
                    createdAt = now - (3600000 * 2) // 2 hours ago
                )
            )
            requestDao.insertRequest(
                MaintenanceRequest(
                    entityId = ent2Id,
                    serviceCategory = "HVAC",
                    description = "Air conditioning is blowing warm air on the entire 3rd floor server side.",
                    priority = "High",
                    status = "Assigned",
                    assignedTo = "HVAC Tech Vikram",
                    location = "Block A, 3rd Floor",
                    createdAt = now - (3600000 * 12) // 12 hours ago
                )
            )
            requestDao.insertRequest(
                MaintenanceRequest(
                    entityId = ent2Id,
                    serviceCategory = "Internet/Network",
                    description = "Wi-Fi signals dropping constantly in conference room 3B.",
                    priority = "Medium",
                    status = "Resolved",
                    assignedTo = "IT Tech Sunil",
                    location = "Block B, 3rd Floor",
                    createdAt = now - (3600000 * 24), // 24 hours ago
                    resolvedAt = now - (3600000 * 20),
                    feedbackRating = 5,
                    feedbackComment = "Sunil resolved it quickly! Bad router replaced."
                )
            )
            requestDao.insertRequest(
                MaintenanceRequest(
                    entityId = ent3Id,
                    serviceCategory = "Road/Infra",
                    description = "Huge pothole causing traffic slowdowns near the main avenue intersection.",
                    priority = "High",
                    status = "Assigned",
                    assignedTo = "Civil Lead Manoj",
                    location = "Avenue 4 Intersection",
                    createdAt = now - (3600000 * 3) // 3 hours ago
                )
            )

            // Seed Scheduled Tasks (Preventive Maintenance)
            scheduleDao.insertScheduledTasks(
                listOf(
                    ScheduledTask(
                        entityId = ent1Id,
                        serviceCategory = "Elevator",
                        description = "Monthly safety inspection & rope lubrication",
                        frequency = "Monthly",
                        nextDueDate = now + (86400000 * 10), // in 10 days
                        assignedTo = "Technician Amit"
                    ),
                    ScheduledTask(
                        entityId = ent1Id,
                        serviceCategory = "Water",
                        description = "Water pump filtration tank chemical cleaning & flush",
                        frequency = "Quarterly",
                        nextDueDate = now + (86400000 * 25), // in 25 days
                        assignedTo = "Plumber Rajesh"
                    ),
                    ScheduledTask(
                        entityId = ent2Id,
                        serviceCategory = "Generator",
                        description = "Weekly load test & battery electrolyte level check",
                        frequency = "Weekly",
                        nextDueDate = now + (86400000 * 3), // in 3 days
                        assignedTo = "Generator Tech Anil"
                    ),
                    ScheduledTask(
                        entityId = ent2Id,
                        serviceCategory = "HVAC",
                        description = "Quarterly replacement of central AC air filters",
                        frequency = "Quarterly",
                        nextDueDate = now + (86400000 * 15), // in 15 days
                        assignedTo = "HVAC Tech Vikram"
                    ),
                    ScheduledTask(
                        entityId = ent3Id,
                        serviceCategory = "Gas",
                        description = "Pressure safety valve tests on municipal trunk pipeline",
                        frequency = "Monthly",
                        nextDueDate = now + (86400000 * 8), // in 8 days
                        assignedTo = "Gas Expert Rahul"
                    )
                )
            )

            // Seed Notifications
            notificationDao.insertNotification(
                NotificationModel(
                    title = "Cascading Alert: Power Interruption",
                    message = "Apex Corporate Software Park: Main Grid Power Cut reported. Automatic failover to Generators triggered. HVAC systems set to economy mode.",
                    type = "CascadingAlert",
                    timestamp = now - (3600000 * 1),
                    isRead = false
                )
            )
            notificationDao.insertNotification(
                NotificationModel(
                    title = "Preventive Work Due soon",
                    message = "Greenwood Residential Society: Monthly Elevator check is due in 10 days.",
                    type = "ScheduledReminder",
                    timestamp = now - (3600000 * 4),
                    isRead = false
                )
            )
        }
    }

    // Database manipulation suspends
    suspend fun insertRequest(request: MaintenanceRequest): Long = withContext(Dispatchers.IO) {
        requestDao.insertRequest(request)
    }

    suspend fun updateRequestStatus(requestId: Long, status: String) = withContext(Dispatchers.IO) {
        if (status == "Resolved" || status == "Verified") {
            requestDao.resolveRequest(requestId, status, System.currentTimeMillis())
        } else {
            requestDao.updateRequestStatus(requestId, status)
        }
    }

    suspend fun updateRequestAssignment(requestId: Long, assignedTo: String, status: String) = withContext(Dispatchers.IO) {
        requestDao.updateRequestAssignment(requestId, assignedTo, status)
    }

    suspend fun updateRequestFeedback(requestId: Long, rating: Int, comment: String) = withContext(Dispatchers.IO) {
        requestDao.updateRequestFeedback(requestId, rating, comment)
    }

    suspend fun deleteRequest(request: MaintenanceRequest) = withContext(Dispatchers.IO) {
        requestDao.deleteRequest(request)
    }

    suspend fun insertService(service: ServiceModel): Long = withContext(Dispatchers.IO) {
        serviceDao.insertService(service)
    }

    suspend fun updateServiceStatusAndCascading(entityId: Long, category: String, status: String) = withContext(Dispatchers.IO) {
        // First find the service
        val services = serviceDao.getAllServices().first()
        val targetService = services.find { it.entityId == entityId && it.category == category }
        if (targetService != null) {
            serviceDao.updateServiceStatus(targetService.id, status)

            // Calculate cascading impact
            // Check which other services depend on this service category
            val entityServices = services.filter { it.entityId == entityId }
            val impactedList = mutableListOf<String>()

            entityServices.forEach { otherService ->
                val deps = otherService.dependencies.split(",").map { it.trim() }
                if (deps.contains(category)) {
                    impactedList.add(otherService.category)
                    // If the parent is Down (Red), the child is often downgraded to Down or Maintenance
                    if (status == "Down") {
                        serviceDao.updateServiceStatus(otherService.id, "Down")
                    } else if (status == "Maintenance") {
                        serviceDao.updateServiceStatus(otherService.id, "Maintenance")
                    } else if (status == "Healthy") {
                        // Reset child to Healthy only if its other dependencies are met
                        serviceDao.updateServiceStatus(otherService.id, "Healthy")
                    }
                }
            }

            // Create notification if status changed to critical/down
            if (status == "Down" && impactedList.isNotEmpty()) {
                val entityName = entityDao.getAllEntities().first().find { it.id == entityId }?.name ?: "Entity"
                val impactMsg = if (impactedList.size == 1) {
                    "This directly impacts: ${impactedList.first()}"
                } else {
                    "This directly impacts: ${impactedList.joinToString(", ")}"
                }
                notificationDao.insertNotification(
                    NotificationModel(
                        title = "Cascading Impact: $category is DOWN",
                        message = "$entityName: $category has failure/power cut. $impactMsg.",
                        type = "CascadingAlert",
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                )
            }
        }
    }

    suspend fun insertScheduledTask(task: ScheduledTask): Long = withContext(Dispatchers.IO) {
        scheduleDao.insertScheduledTask(task)
    }

    suspend fun performScheduledTask(taskId: Long) = withContext(Dispatchers.IO) {
        val tasks = scheduleDao.getAllScheduledTasks().first()
        val task = tasks.find { it.id == taskId }
        if (task != null) {
            val now = System.currentTimeMillis()
            val nextDue = when (task.frequency) {
                "Weekly" -> now + (86400000 * 7)
                "Monthly" -> now + (86400000 * 30)
                "Quarterly" -> now + (86400000 * 91)
                else -> now + (86400000 * 30)
            }
            scheduleDao.updateScheduledTaskLastPerformed(taskId, now, nextDue)

            // Log a notification
            notificationDao.insertNotification(
                NotificationModel(
                    title = "Preventive Work Done",
                    message = "${task.serviceCategory} preventive maintenance completed successfully by ${task.assignedTo}.",
                    type = "StatusUpdate",
                    timestamp = now,
                    isRead = false
                )
            )
        }
    }

    suspend fun insertNotification(notification: NotificationModel) = withContext(Dispatchers.IO) {
        notificationDao.insertNotification(notification)
    }

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun clearNotifications() = withContext(Dispatchers.IO) {
        notificationDao.clearAllNotifications()
    }
}
