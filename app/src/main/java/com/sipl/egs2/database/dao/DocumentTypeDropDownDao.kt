package com.sipl.egs2.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sipl.egs2.database.entity.DocumentTypeDropDown

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
    @Query("SELECT COUNT(*) FROM document_type_dropdown WHERE is_active=1")
    suspend fun getRowCount(): Int
    @Query("SELECT * FROM document_type_dropdown WHERE is_active=1")
    suspend fun getAllDocumentsType(): List<DocumentTypeDropDown>

    @Query("SELECT * FROM document_type_dropdown WHERE id = :id AND is_active=1")
    suspend fun getDocumentTypeById(id: String): DocumentTypeDropDown

    @Query("SELECT * FROM document_type_dropdown WHERE is_active=1 ORDER BY documenttype ASC")
    suspend fun getDocuments(): List<DocumentTypeDropDown>

}