package com.sipl.egs2.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_type")
data class DocumentType(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    var documentName: String,
    var isAdded:Boolean,
    var  isSynced: Boolean
)