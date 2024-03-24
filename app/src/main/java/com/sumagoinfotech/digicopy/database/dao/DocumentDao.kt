package com.sumagoinfotech.digicopy.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sumagoinfotech.digicopy.database.entity.Document
import com.sumagoinfotech.digicopy.database.models.DocumentCount




@Dao
interface DocumentDao {
    @Insert
    suspend fun insertDocument(document: Document) : Long

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("SELECT * FROM documents WHERE isSynced=false ORDER BY id DESC ")
    fun getAllDocuments(): List<Document>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getUserById(id: Int): Document

    @Query("SELECT COUNT(*) FROM documents WHERE isSynced=false")
    suspend fun getDocumentsCount(): Int

/*    @Query("SELECT documentName, COUNT(*) as count FROM documents WHERE isSynced = 0 GROUP BY documentName ")
    suspend fun getDocumentCountByName(): List<DocumentCount?>?*/
}