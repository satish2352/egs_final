package com.sumagoinfotech.digicopy.interfaces

import com.sumagoinfotech.digicopy.database.entity.Labour

interface MarkAttendanceListener {
    fun markAttendance(labour : Labour)
}