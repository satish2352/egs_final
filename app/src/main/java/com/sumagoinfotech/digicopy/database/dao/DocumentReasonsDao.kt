package com.sumagoinfotech.digicopy.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sumagoinfotech.digicopy.database.entity.DocumentReasons
import com.sumagoinfotech.digicopy.database.entity.Reasons

@Dao
interface DocumentReasonsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<DocumentReasons>)

    @Query("DELETE FROM reasons")
    suspend fun deleteAllReasons()
    @Transaction
    suspend fun insertInitialRecords(items: List<DocumentReasons>) {
        deleteAllReasons()
        insertAll(items)
    }
    @Query("SELECT * FROM reasons ORDER BY id ASC")
    suspend fun getAllReasons(): List<DocumentReasons>
    @Query("SELECT * FROM reasons WHERE id = :id")
    suspend fun getReasonById(id: String): DocumentReasons
}