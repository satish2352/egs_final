package com.sumagoinfotech.digicopy.interfaces

import com.sumagoinfotech.digicopy.database.entity.Labour
import com.sumagoinfotech.digicopy.model.apis.getlabour.LabourInfo

interface MarkAttendanceListener {
    fun markAttendance(labour : LabourInfo)
}