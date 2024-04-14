package com.sipl.egs.interfaces

import com.sipl.egs.database.entity.Document

interface UpdateDocumentTypeListener {
    fun onUpdateDocumentType(documentName: Document)
}