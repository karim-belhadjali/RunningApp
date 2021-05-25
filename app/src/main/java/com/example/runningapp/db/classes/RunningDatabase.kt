package com.example.runningapp.db.classes

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.runningapp.db.converters.Converters
import com.example.runningapp.db.dao.RunDAO

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RunningDatabase : RoomDatabase() {

    abstract fun getRunDao() :RunDAO
}