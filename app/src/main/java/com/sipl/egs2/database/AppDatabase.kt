package com.sipl.egs2.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RenameTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sipl.egs2.database.dao.AreaDao
import com.sipl.egs2.database.dao.DocumentDao
import com.sipl.egs2.database.dao.DocumentReasonsDao
import com.sipl.egs2.database.dao.DocumentTypeDropDownDao
import com.sipl.egs2.database.dao.GenderDao
import com.sipl.egs2.database.dao.LabourDao
import com.sipl.egs2.database.dao.MaritalStatusDao
import com.sipl.egs2.database.dao.ReasonsDao
import com.sipl.egs2.database.dao.RegistrationStatusDao
import com.sipl.egs2.database.dao.RelationDao
import com.sipl.egs2.database.dao.SkillsDao
import com.sipl.egs2.database.dao.UserDao
import com.sipl.egs2.database.entity.AreaItem
import com.sipl.egs2.database.entity.Document
import com.sipl.egs2.database.entity.DocumentReasons
import com.sipl.egs2.database.entity.DocumentType
import com.sipl.egs2.database.entity.DocumentTypeDropDown
import com.sipl.egs2.database.entity.Gender
import com.sipl.egs2.database.entity.Labour
import com.sipl.egs2.database.entity.MaritalStatus
import com.sipl.egs2.database.entity.Reasons
import com.sipl.egs2.database.entity.RegistrationStatus
import com.sipl.egs2.database.entity.Relation
import com.sipl.egs2.database.entity.Skills
import com.sipl.egs2.database.entity.User

@DeleteTable.Entries(
    DeleteTable(
        tableName = "document_type"
    )
)
@RenameTable.Entries(
    RenameTable(
        fromTableName = "document_type",
        toTableName = "document_type_new"
    )
)
@Database(entities = [Labour::class,Document::class,User::class,AreaItem::class,Skills::class,MaritalStatus::class, DocumentType::class,Relation::class,Gender::class,DocumentTypeDropDown::class,RegistrationStatus::class,Reasons::class,DocumentReasons::class], version = 13,
    autoMigrations = [AutoMigration(from = 12 , to = 13)],exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun labourDao(): LabourDao
    abstract fun documentDao(): DocumentDao
    abstract fun userDao(): UserDao
    abstract fun areaDao(): AreaDao
    abstract fun skillsDao(): SkillsDao
    abstract fun martialStatusDao(): MaritalStatusDao
    abstract fun relationDao():RelationDao
    abstract fun genderDao():GenderDao
    abstract fun documentDropDownDao():DocumentTypeDropDownDao
    abstract fun registrationStatusDao():RegistrationStatusDao
    abstract fun reasonsDao():ReasonsDao
    abstract fun documentsReasonsDao():DocumentReasonsDao

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
                    }
                })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}