package com.sumagoinfotech.digicopy.interfaces

import com.sumagoinfotech.digicopy.database.entity.Document

interface UpdateDocumentTypeListener {
    fun onUpdateDocumentType(documentName: Document)
}