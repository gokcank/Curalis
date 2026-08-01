package com.gokcank.curalis.data.provider.openfda

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApi {

    @GET("drug/ndc.json")
    suspend fun searchMedications(
        @Query("search") search: String,
        @Query("limit") limit: Int = 10
    ): OpenFdaResponse

    companion object {
        const val BASE_URL = "https://api.fda.gov/"
    }
}
