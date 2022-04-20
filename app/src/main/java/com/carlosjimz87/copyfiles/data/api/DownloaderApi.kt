package com.carlosjimz87.copyfiles.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DownloaderApi {
    @GET("/{type}/{id}/download/")
    suspend fun getFile(
        @Path("type") type: String,
        @Path("id") id: Long
    ): Response<ResponseBody>
}