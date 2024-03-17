package com.sumagoinfotech.digicopy.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sumagoinfotech.digicopy.database.entity.Gender
import com.sumagoinfotech.digicopy.database.entity.MaritalStatus
import com.sumagoinfotech.digicopy.database.entity.Relation

@Dao
interface RelationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<Relation>)

    @Query("DELETE FROM relation ")
    suspend fun deleteAllRelations()

    @Transaction
    suspend fun insertInitialRecords(items: List<Relation>) {
        deleteAllRelations()
        insertAll(items)
    }

    @Query("SELECT * FROM relation ORDER BY relation_title ASC")
    suspend fun getAllRelation(): List<Relation>

    @Query("SELECT * FROM relation WHERE id = :id")
    suspend fun getRelationById(id: String): Relation
}