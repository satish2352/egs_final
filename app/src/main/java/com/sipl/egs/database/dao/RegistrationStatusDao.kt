package com.sipl.egs.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sipl.egs.database.entity.RegistrationStatus

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
        @Query("SELECT * FROM registration_status ORDER BY id ASC")
        suspend fun getAllRegistrationStatus(): List<RegistrationStatus>
        @Query("SELECT * FROM registration_status WHERE id = :id")
        suspend fun getRegistrationStatusById(id: String): RegistrationStatus
}