package com.sipl.egs.interfaces

import com.sipl.egs.model.apis.getlabour.LabourInfo

interface MarkAttendanceListener {
    fun markAttendance(labour : LabourInfo)
}