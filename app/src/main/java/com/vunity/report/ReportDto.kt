package com.vunity.report

data class ReportDto(
    var contentFound: Boolean?,
    var `data`: List<ReportData>?,
    var message: String?,
    var status: Int?
)

data class ReportData(
    var activeUsers: Int?,
    var date: String?
)


data class ListReportData(
    var activeUsers: Int,
    var date: String,
    var color: Int
)