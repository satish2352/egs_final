package com.sipl.egs2.interfaces

import com.sipl.egs2.model.apis.getlabour.LabourInfo

interface MarkAttendanceListener {
    fun markAttendance(labour : LabourInfo)
}