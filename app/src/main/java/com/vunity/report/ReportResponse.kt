package com.vunity.report

import retrofit2.Call
import retrofit2.http.GET

interface ReportResponse {

    @GET("cms/active_user_report")
    fun cmsReport(): Call<ReportDto>

}