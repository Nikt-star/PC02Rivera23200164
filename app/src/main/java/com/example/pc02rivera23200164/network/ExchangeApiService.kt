package com.example.pc02rivera23200164.network

import com.example.pc02rivera23200164.model.ExchangeRateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeApiService {
    @GET("v6/{apiKey}/latest/{base}")
    suspend fun getLatestRates(
        @Path("apiKey") apiKey: String = "6b0500a4167f16b7cb52327c",
        @Path("base") base: String
    ): Response<ExchangeRateResponse>
}
