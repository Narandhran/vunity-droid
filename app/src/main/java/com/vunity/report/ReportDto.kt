package com.vunity.report

data class ReportDto(
    var contentFound: Boolean?,
    var `data`: CMSReport?,
    var message: String?,
    var status: Int?
)

data class CMSReport(
    var activeUser: Int?,
    var report: List<ReportData>?,
    var totalUser: Int?
)

data class ReportData(
    var activeUsers: Int,
    var date: String
)