package com.sumagoinfotech.digicopy.interfaces

import com.sumagoinfotech.digicopy.model.apis.attendance.AttendanceData

interface AttendanceEditListener {

    fun onAttendanceEdit(data: AttendanceData,postion:Int)
}