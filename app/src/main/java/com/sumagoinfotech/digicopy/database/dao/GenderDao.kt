package com.sumagoinfotech.digicopy.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sumagoinfotech.digicopy.database.entity.AreaItem
import com.sumagoinfotech.digicopy.database.entity.Gender
import com.sumagoinfotech.digicopy.database.entity.Relation

@Dao
interface GenderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<Gender>)

    @Query("DELETE FROM gender")
    suspend fun deleteAllGenders()
    @Transaction
    suspend fun insertInitialRecords(items: List<Gender>) {
        deleteAllGenders()
        insertAll(items)
    }

    @Query("SELECT * FROM gender ORDER BY gender_name ASC")
    suspend fun getAllGenders(): List<Gender>

    @Query("SELECT * FROM gender WHERE id = :id")
    suspend fun getGenderById(id: String): Gender

}