package com.sipl.egs2.utils

import java.util.regex.Pattern

object MyValidator {

    public fun isValidName(name:String):Boolean{
        var result = name!==null && name.isNotEmpty() && name.isNotBlank() && name.length>4
        return result
    }
    public fun isValidEmailX(email:String):Boolean{
        var result = email!==null && email.isNotEmpty() && email.isNotBlank() && email.length==10
        return result
    }
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        val pattern = Pattern.compile(android.util.Patterns.EMAIL_ADDRESS.toString())
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }
    public fun isValidMobileNumber(mobileNumber: String): Boolean {
        val regex = "^[6-9]\\d{9}$"
        return mobileNumber.matches(Regex(regex))
    }

    fun isValidPassword(password: String): Boolean {
        return password!==null && password.isNotEmpty() && password.isNotBlank() && password.length==8

    }
    fun isValidConfirmPassword(password: String,confirmPassword:String): Boolean {
        return password!==null && password.isNotEmpty() && password.isNotBlank() && password.length==8 && password.equals(confirmPassword)

    }
    fun isValidPasswordOld(password: String): Boolean {
        return password!==null && password.isNotEmpty() && password.isNotBlank() && password.length>=8

    }

    fun isValidMgnregaId(mgnregaId: String): Boolean {
        return mgnregaId!==null && mgnregaId.isNotEmpty() && mgnregaId.isNotBlank() && mgnregaId.length==10

    }

    fun isValidPasswordPattern(password: String): Boolean {
        val pattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$")
        return pattern.matches(password)
    }

}