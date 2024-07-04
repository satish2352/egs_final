package com.sipl.egs2.model.apis.mastersupdate

data class AreaMastersUpdateModel(
    val `data`: List<AreaMaster>,
    val message: String,
    val status: String
)