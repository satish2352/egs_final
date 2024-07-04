package com.sipl.egs2.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sipl.egs2.database.entity.RegistrationStatus

@Dao
interface RegistrationStatusDao {
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        suspend fun insertAll(items: List<RegistrationStatus>)

        @Query("DELETE FROM registration_status")
        suspend fun deleteAllStatus()
        @Transaction
        suspend fun insertInitialRecords(items: List<RegistrationStatus>) {
            deleteAllStatus()
            insertAll(items)
        }
        @Query("SELECT COUNT(*) FROM registration_status WHERE  is_active=1")
        suspend fun getRowCount(): Int
        @Query("SELECT * FROM registration_status WHERE  is_active=1 ORDER BY id ASC")
        suspend fun getAllRegistrationStatus(): List<RegistrationStatus>
        @Query("SELECT * FROM registration_status WHERE id = :id AND is_active=1")
        suspend fun getRegistrationStatusById(id: String): RegistrationStatus
}