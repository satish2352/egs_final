package com.sumagoinfotech.digicopy.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sumagoinfotech.digicopy.database.entity.DocumentType

@Dao
interface DocumentTypeDao {
    @Insert
    suspend fun insertDocumentType(document: DocumentType) : Long

    @Update
    suspend fun updateDocumentType(document: DocumentType)

    @Delete
    suspend fun deleteDocumentType(documentType: DocumentType)

    @Query("SELECT * FROM document_type")
    fun getDocuments(): List<DocumentType>

    @Query("SELECT * FROM document_type WHERE id = :id")
    suspend fun getUserById(id: Int): DocumentType

    @Query("SELECT * FROM document_type WHERE isAdded = 0")
    fun getDocumentsNotAdded(): List<DocumentType>

    @Query("SELECT * FROM document_type WHERE documentName = :name")
    suspend fun getDocumentByName(name: String): DocumentType?
    @Transaction
    suspend fun insertInitialRecords() {
        // Check if any records exist
        if (getDocuments().isEmpty()) {
            // Insert initial records
            insertDocumentType(DocumentType(documentName = "7/12 Extract",  isAdded = false, isSynced = false))
            insertDocumentType(DocumentType(documentName = "Namuna 8",  isAdded = false, isSynced = false))
            insertDocumentType(DocumentType(documentName = "Namuna 8A",  isAdded = false, isSynced = false))

        }
    }
}