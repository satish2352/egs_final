package com.sipl.egs.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RenameTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sipl.egs.database.dao.AreaDao
import com.sipl.egs.database.dao.DocumentDao
import com.sipl.egs.database.dao.DocumentReasonsDao
import com.sipl.egs.database.dao.DocumentTypeDropDownDao
import com.sipl.egs.database.dao.GenderDao
import com.sipl.egs.database.dao.LabourDao
import com.sipl.egs.database.dao.MaritalStatusDao
import com.sipl.egs.database.dao.ReasonsDao
import com.sipl.egs.database.dao.RegistrationStatusDao
import com.sipl.egs.database.dao.RelationDao
import com.sipl.egs.database.dao.SkillsDao
import com.sipl.egs.database.dao.UserDao
import com.sipl.egs.database.entity.AreaItem
import com.sipl.egs.database.entity.Document
import com.sipl.egs.database.entity.DocumentReasons
import com.sipl.egs.database.entity.DocumentType
import com.sipl.egs.database.entity.DocumentTypeDropDown
import com.sipl.egs.database.entity.Gender
import com.sipl.egs.database.entity.Labour
import com.sipl.egs.database.entity.MaritalStatus
import com.sipl.egs.database.entity.Reasons
import com.sipl.egs.database.entity.RegistrationStatus
import com.sipl.egs.database.entity.Relation
import com.sipl.egs.database.entity.Skills
import com.sipl.egs.database.entity.User

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
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}