package com.sumagoinfotech.digicopy.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sumagoinfotech.digicopy.database.entity.MaritalStatus
import com.sumagoinfotech.digicopy.database.entity.Skills

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

    @Query("SELECT * FROM marital_status ORDER BY id ASC")
    suspend fun getAllMaritalStatus(): List<MaritalStatus>

    @Query("SELECT * FROM marital_status WHERE id = :id")
    suspend fun getMaritalStatusById(id: String): MaritalStatus
}