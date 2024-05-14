package com.sipl.egs.model.apis.skills

data class SkillsModel(
    val `data`: List<SkillsData>,
    val message: String,
    val status: String
)