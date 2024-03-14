package com.sumagoinfotech.digicopy.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sumagoinfotech.digicopy.database.dao.AreaDao
import com.sumagoinfotech.digicopy.database.dao.DocumentDao
import com.sumagoinfotech.digicopy.database.dao.DocumentTypeDao
import com.sumagoinfotech.digicopy.database.dao.LabourDao
import com.sumagoinfotech.digicopy.database.dao.UserDao
import com.sumagoinfotech.digicopy.database.entity.AreaItem
import com.sumagoinfotech.digicopy.database.entity.Document
import com.sumagoinfotech.digicopy.database.entity.DocumentType
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.database.entity.User
import java.util.concurrent.Executors

@Database(entities = [Labour::class,Document::class,DocumentType::class,User::class,AreaItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun labourDao(): LabourDao
    abstract fun documentDao(): DocumentDao
    abstract fun documentTypeDao(): DocumentTypeDao
    abstract fun userDao(): UserDao
    abstract fun areaDao(): AreaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).addCallback(object : RoomDatabase.Callback(){
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Executors.newSingleThreadScheduledExecutor().execute {
                           /* CoroutineScope(Dispatchers.IO).launch {
                                getDatabase(context).documentTypeDao().insertInitialRecords()
                                getDatabase(context).userDao().insertInitialRecords()
                            }*/
                        }
                    }
                })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}