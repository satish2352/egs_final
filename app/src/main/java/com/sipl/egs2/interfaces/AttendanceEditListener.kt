package com.sipl.egs2.interfaces

import com.sipl.egs2.model.apis.attendance.AttendanceData

interface AttendanceEditListener {

    fun onAttendanceEdit(data: AttendanceData,postion:Int)
}