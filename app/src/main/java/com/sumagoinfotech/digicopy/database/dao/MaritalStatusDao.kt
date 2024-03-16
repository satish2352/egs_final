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
}