package com.sipl.egs.model.apis.mastersupdate

data class AreaMastersUpdateModel(
    val `data`: List<AreaMaster>,
    val message: String,
    val status: String
)