package com.sumagoinfotech.digicopy.database.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sumagoinfotech.digicopy.database.entity.AreaItem
import com.sumagoinfotech.digicopy.database.entity.Relation
import com.sumagoinfotech.digicopy.database.entity.Skills

@Dao
interface SkillsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<Skills>)

    @Query("DELETE FROM skills")
    suspend fun deleteAllSkills()
    @Transaction
    suspend fun insertInitialRecords(items: List<Skills>) {
        deleteAllSkills()
            insertAll(items)
    }
    @Query("SELECT * FROM skills ORDER BY skills ASC")
    suspend fun getAllSkills(): List<Skills>

    @Query("SELECT * FROM skills WHERE id = :id")
    suspend fun getSkillById(id: String): Skills
}