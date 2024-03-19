package com.sumagoinfotech.digicopy.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.database.model.LabourWithAreaNames

@Dao
interface LabourDao {
    @Insert
    suspend fun insertLabour(labour: Labour) : Long

    @Update
    suspend fun updateLabour(labour: Labour):Int

    @Delete
    suspend fun deleteLabour(labour: Labour)

    @Query("SELECT * FROM labours WHERE isSynced=false")
    fun getAllLabour(): List<Labour>

    @Query("SELECT * FROM labours WHERE id = :id")
    suspend fun getLabourById(id: Int): Labour

    @Query("SELECT * FROM labours WHERE mgnregaId = :mgnregaId")
    suspend fun getLabourByMgnregaId(mgnregaId: String): Labour

    @Query("SELECT * FROM labours WHERE mgnregaId like '%' || :searchQuery || '%'")
    suspend fun getLabourByMgnregaIdLike(searchQuery: String): List<Labour>

    @Query("SELECT COUNT(*) FROM labours WHERE isSynced=false")
    suspend fun getLaboursCount(): Int

    @Query("SELECT l.*, village.name AS villageName, district.name AS districtName, taluka.name AS talukaName " +
            "FROM labours l " +
            "LEFT JOIN area AS village ON l.village = village.location_id " +
            "LEFT JOIN area AS district ON l.district = district.location_id " +
            "LEFT JOIN area AS taluka ON l.taluka = taluka.location_id WHERE isSynced=false ORDER BY id DESC")
    suspend fun getLabourWithAreaNames(): List<LabourWithAreaNames>


    @Query("SELECT l.*, village.name AS villageName, district.name AS districtName, taluka.name AS talukaName " +
            "FROM labours l " +
            "LEFT JOIN area AS village ON l.village = village.location_id " +
            "LEFT JOIN area AS district ON l.district = district.location_id " +
            "LEFT JOIN area AS taluka ON l.taluka = taluka.location_id " +
            "WHERE l.id = :labourId AND isSynced=false")
    suspend fun getLabourWithAreaNamesById(labourId: Int): LabourWithAreaNames?
}