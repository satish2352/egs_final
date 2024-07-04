package com.sipl.egs2.interfaces

import com.sipl.egs2.database.entity.Document

interface UpdateDocumentTypeListener {
    fun onUpdateDocumentType(documentName: Document)
}