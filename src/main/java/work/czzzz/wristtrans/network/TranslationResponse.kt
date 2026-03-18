package work.czzzz.wisttrans.network

import com.google.gson.annotations.SerializedName

data class TranslationResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("original")
    val original: String,
    @SerializedName("translated")
    val translated: String,
    @SerializedName("from")
    val from: String
)
