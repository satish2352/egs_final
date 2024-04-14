package com.sipl.egs.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sipl.egs.database.entity.DocumentTypeDropDown

@Dao
interface DocumentTypeDropDownDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<DocumentTypeDropDown>)

    @Query("DELETE FROM document_type_dropdown")
    suspend fun deleteAllDocuementTypes()
    @Transaction
    suspend fun insertInitialRecords(items: List<DocumentTypeDropDown>) {
        deleteAllDocuementTypes()
        insertAll(items)
    }
    @Query("SELECT * FROM document_type_dropdown")
    suspend fun getAllDocumentsType(): List<DocumentTypeDropDown>

    @Query("SELECT * FROM document_type_dropdown WHERE id = :id")
    suspend fun getDocumentTypeById(id: String): DocumentTypeDropDown

    @Query("SELECT * FROM document_type_dropdown ORDER BY documenttype ASC")
    suspend fun getDocuments(): List<DocumentTypeDropDown>

}