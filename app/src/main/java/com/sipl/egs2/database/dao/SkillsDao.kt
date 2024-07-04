package com.sipl.egs2.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sipl.egs2.database.entity.Skills

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

    @Query("SELECT COUNT(*) FROM skills WHERE  is_active=1")
    suspend fun getRowCount(): Int
    @Query("SELECT * FROM skills WHERE  is_active=1 ORDER BY id ASC ")
    suspend fun getAllSkills(): List<Skills>

    @Query("SELECT * FROM skills WHERE id = :id AND is_active=1")
    suspend fun getSkillById(id: String): Skills
}