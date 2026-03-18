package work.czzzz.wristtrans.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import work.czzzz.wristtrans.data.network.TranslationApi
import work.czzzz.wristtrans.ui.model.TranslationHistoryItem
import work.czzzz.wristtrans.ui.model.TranslationUiState
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class WristTransViewModel : ViewModel() {
    private val service = TranslationApi.service
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val _uiState = MutableStateFlow(TranslationUiState())
    val uiState: StateFlow<TranslationUiState> = _uiState.asStateFlow()

    fun translate(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val sourceLanguage = _uiState.value.sourceLanguage
        val targetLanguage = _uiState.value.targetLanguage

        _uiState.update {
            it.copy(
                lastInput = trimmed,
                isLoading = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            runCatching {
                service.translate(
                    query = trimmed,
                    targetLanguage = targetLanguage,
                    sourceLanguage = sourceLanguage,
                )
            }.onSuccess { response ->
                if (response.status != "success") {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Translation service returned an invalid status.",
                        )
                    }
                    return@onSuccess
                }

                val historyItem =
                    TranslationHistoryItem(
                        id = UUID.randomUUID().toString(),
                        original = response.original,
                        translated = response.translated,
                        from = response.from,
                        to = targetLanguage,
                        createdAt = LocalTime.now().format(timeFormatter),
                    )

                _uiState.update {
                    it.copy(
                        lastTranslation = response.translated,
                        detectedLanguage = response.from,
                        isLoading = false,
                        history = listOf(historyItem) + it.history.take(19),
                    )
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Network request failed.",
                    )
                }
            }
        }
    }

    fun toggleSourceLanguage() {
        _uiState.update { state ->
            state.copy(
                sourceLanguage =
                    when (state.sourceLanguage) {
                        "auto" -> "en"
                        else -> "auto"
                    },
            )
        }
    }

    fun toggleTargetLanguage() {
        _uiState.update { state ->
            state.copy(
                targetLanguage =
                    when (state.targetLanguage) {
                        "zh-CN" -> "en"
                        else -> "zh-CN"
                    },
            )
        }
    }

    fun swapLanguages() {
        _uiState.update { state ->
            if (state.sourceLanguage == "auto") {
                state.copy(sourceLanguage = "en", targetLanguage = "zh-CN")
            } else {
                state.copy(
                    sourceLanguage = state.targetLanguage,
                    targetLanguage = state.sourceLanguage,
                )
            }
        }
    }
}
