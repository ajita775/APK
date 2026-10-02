package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FocusSession
import com.example.data.model.HabitItem
import com.example.data.model.ScheduleItem
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile

@Database(
    entities = [
        TaskItem::class,
        HabitItem::class,
        ScheduleItem::class,
        FocusSession::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TimeFlowDatabase : RoomDatabase() {
    abstract fun timeFlowDao(): TimeFlowDao

    companion object {
        @Volatile
        private var INSTANCE: TimeFlowDatabase? = null

        fun getInstance(context: Context): TimeFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeFlowDatabase::class.java,
                    "timeflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
