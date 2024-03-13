package com.sumagoinfotech.digicopy.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val documentName: String,
    val documentUri: String,
    val pageCount:String,
    val isSynced: Boolean,
    val documentId: String,
)