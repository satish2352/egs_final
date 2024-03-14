package com.sumagoinfotech.digicopy.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sumagoinfotech.digicopy.database.entity.AreaItem
import com.sumagoinfotech.digicopy.database.entity.Document

@Dao
interface AreaDao {

    @Insert
    suspend fun insertArea(areaItem: AreaItem) : Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(items: List<AreaItem>)
    @Query("SELECT * FROM area WHERE location_type=2")
    suspend fun getAllDistrict(): List<AreaItem>

    @Query("SELECT * FROM area WHERE location_type=3  AND  parent_id = :id")
    suspend fun getTalukaByDistrict(id: Int): List<AreaItem>

    @Query("SELECT * FROM area WHERE location_type=4  AND  parent_id = :id")
    suspend fun getVillageByTaluka(id: Int): List<AreaItem>
}