package com.sumagoinfotech.digicopy.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sumagoinfotech.digicopy.database.entity.Document

@Dao
interface DocumentDao {
    @Insert
    suspend fun insertDocument(document: Document) : Long

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("SELECT * FROM documents")
    fun getAllUsers(): List<Document>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getUserById(id: Int): Document
}