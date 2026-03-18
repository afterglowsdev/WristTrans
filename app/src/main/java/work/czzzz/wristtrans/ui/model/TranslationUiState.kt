package work.czzzz.wristtrans.ui.model

data class TranslationUiState(
    val lastInput: String = "",
    val lastTranslation: String = "",
    val sourceLanguage: String = "auto",
    val targetLanguage: String = "zh-CN",
    val detectedLanguage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val history: List<TranslationHistoryItem> = emptyList(),
)

data class TranslationHistoryItem(
    val id: String,
    val original: String,
    val translated: String,
    val from: String,
    val to: String,
    val createdAt: String,
)

