package com.example.querico.Model.ModelRoom

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.querico.RicoApplication
import com.example.querico.Model.ModelRoom.Dao.PostDao
import com.example.querico.Model.ModelRoom.Dao.UserDao
import com.example.querico.Model.Entities.PostEntity
import com.example.querico.Model.Entities.UserEntity

@Database(entities = [UserEntity::class, PostEntity::class], version = 2, exportSchema = false)
abstract class AppDB : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao

    companion object {
        // Define a singleton instance of the database
        @Volatile private var instance: AppDB? = null;
        private const val DB_NAME = "QUE_RICO"

        fun getInstance(): AppDB {
            return instance?: synchronized(this) {
                instance?: Room.databaseBuilder(
                    RicoApplication.getInstance().applicationContext,
                    AppDB::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
        }
    }
}