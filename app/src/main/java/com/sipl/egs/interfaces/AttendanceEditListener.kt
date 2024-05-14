package com.sipl.egs.interfaces

import com.sipl.egs.model.apis.attendance.AttendanceData

interface AttendanceEditListener {

    fun onAttendanceEdit(data: AttendanceData,postion:Int)
}