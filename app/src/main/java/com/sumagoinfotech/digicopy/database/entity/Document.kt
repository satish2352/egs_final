package com.sumagoinfotech.digicopy.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val documentName: String,
    var documentUri: String,
    var pageCount:String,
    var isSynced: Boolean,
    var documentId: String,
)