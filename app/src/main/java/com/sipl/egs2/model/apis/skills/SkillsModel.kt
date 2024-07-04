package com.sipl.egs2.model.apis.skills

data class SkillsModel(
    val `data`: List<SkillsData>,
    val message: String,
    val status: String
)