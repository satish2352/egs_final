package com.sumagoinfotech.digicopy.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sumagoinfotech.digicopy.database.entity.Labour

@Dao
interface LabourDao {
    @Insert
    suspend fun insertLabour(labour: Labour) : Long

    @Update
    suspend fun updateLabour(labour: Labour)

    @Delete
    suspend fun deleteLabour(labour: Labour)

    @Query("SELECT * FROM labours")
    fun getAllLabour(): List<Labour>

    @Query("SELECT * FROM labours WHERE id = :id")
    suspend fun getLabourById(id: Int): Labour

    @Query("SELECT * FROM labours WHERE mgnregaId = :mgnregaId")
    suspend fun getLabourByMgnregaId(mgnregaId: String): Labour

    @Query("SELECT * FROM labours WHERE mgnregaId like '%' || :searchQuery || '%'")
    suspend fun getLabourByMgnregaIdLike(searchQuery: String): List<Labour>

}