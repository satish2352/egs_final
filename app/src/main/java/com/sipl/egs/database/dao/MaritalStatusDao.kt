package com.sipl.egs.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sipl.egs.database.entity.MaritalStatus

@Dao
interface MaritalStatusDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<MaritalStatus>)

    @Query("DELETE FROM marital_status")
    suspend fun deleteAllMaritalSkills()
    @Transaction
    suspend fun insertInitialRecords(items: List<MaritalStatus>) {
        deleteAllMaritalSkills()
        insertAll(items)
    }

    @Query("SELECT COUNT(*) FROM marital_status WHERE  is_active=1")
    suspend fun getRowCount(): Int
    @Query("SELECT * FROM marital_status WHERE  is_active=1 ORDER BY id ASC")
    suspend fun getAllMaritalStatus(): List<MaritalStatus>

    @Query("SELECT * FROM marital_status WHERE id = :id AND is_active=1")
    suspend fun getMaritalStatusById(id: String): MaritalStatus
}