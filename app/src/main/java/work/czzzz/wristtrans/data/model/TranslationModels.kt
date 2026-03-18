package work.czzzz.wristtrans.data.model

data class TranslateQuery(
    val text: String,
    val to: String = "zh-CN",
    val from: String = "auto",
)

data class TranslationResponse(
    val status: String,
    val original: String,
    val translated: String,
    val from: String,
)
