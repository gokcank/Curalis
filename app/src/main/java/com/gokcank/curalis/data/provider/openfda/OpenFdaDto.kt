package com.gokcank.curalis.data.provider.openfda

import com.google.gson.annotations.SerializedName

data class OpenFdaResponse(
    @SerializedName("results")
    val results: List<OpenFdaResult>?
)

data class OpenFdaResult(
    @SerializedName("brand_name")
    val brandName: String?,
    @SerializedName("generic_name")
    val genericName: String?,
    @SerializedName("dosage_form")
    val dosageForm: String?,
    @SerializedName("active_ingredients")
    val activeIngredients: List<OpenFdaActiveIngredient>?
)

data class OpenFdaActiveIngredient(
    @SerializedName("name")
    val name: String?,
    @SerializedName("strength")
    val strength: String?
)
