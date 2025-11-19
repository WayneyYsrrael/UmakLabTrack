package com.example.umaklabtrack.dataClasses

object UserSession {

    var name: String? = null
    var email: String? = null
    var hashedPassword: String? = null
    var cNum: String? = null
    var USER_ID: String? = null

    // Additional fields
    var college: String? = null
    var subject: String? = null
    var yearSection: String? = null
    var pickup: String? = null
    var room: String? = null

    fun clear() {
        USER_ID = null
        name = null
        email = null
        hashedPassword = null
        cNum = null
        college = null
        subject = null
        yearSection = null
    }
}
