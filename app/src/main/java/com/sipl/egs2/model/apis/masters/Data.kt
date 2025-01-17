package com.sipl.egs2.model.apis.masters

data class Data(
    val documenttype: List<Documenttype>,
    val gender: List<Gender>,
    val maritalstatus: List<Maritalstatu>,
    val relation: List<Relation>,
    val skills: List<Skill>,
    val registrationstatus: List<RegistrationStatus>,
    val reasons: List<Reasons>,
    val documentreasons: List<Reasons>,
)