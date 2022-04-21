package com.carlosjimz87.copyfiles.data.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


interface DownloaderApi {

    @Streaming
    @GET
    suspend fun getFile(
        @Url fileUrl: String
    ): Response<ResponseBody>


    @Streaming
    @GET("/{type}/{id}/download/")
    suspend fun getFile(
        @Path("type") type: String,
        @Path("id") id: Long,
        ): Response<ResponseBody>
}