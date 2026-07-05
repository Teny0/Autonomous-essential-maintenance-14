package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        EntityModel::class,
        ServiceModel::class,
        MaintenanceRequest::class,
        ScheduledTask::class,
        NotificationModel::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entityDao(): EntityDao
    abstract fun serviceDao(): ServiceDao
    abstract fun requestDao(): RequestDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun notificationDao(): NotificationDao
}
