package com.sipl.egs2.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sipl.egs2.database.entity.Reasons


@Dao
interface ReasonsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<Reasons>)

    @Query("DELETE FROM reasons")
    suspend fun deleteAllReasons()
    @Transaction
    suspend fun insertInitialRecords(items: List<Reasons>) {
        deleteAllReasons()
        insertAll(items)
    }
    @Query("SELECT COUNT(*) FROM reasons WHERE  is_active=1")
    suspend fun getRowCount(): Int
    @Query("SELECT * FROM reasons WHERE  is_active=1 ORDER BY id ASC")
    suspend fun getAllReasons(): List<Reasons>
    @Query("SELECT * FROM reasons WHERE id = :id AND is_active=1")
    suspend fun getReasonById(id: String): Reasons
}