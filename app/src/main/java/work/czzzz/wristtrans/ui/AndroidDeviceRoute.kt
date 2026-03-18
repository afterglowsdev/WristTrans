package work.czzzz.wristtrans.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import work.czzzz.wristtrans.R
import work.czzzz.wristtrans.ui.model.TranslationHistoryItem
import work.czzzz.wristtrans.ui.model.languageLabel

@Composable
fun AndroidDeviceRoute(
    viewModel: WristTransViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by rememberSaveable { mutableStateOf("") }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.input_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(R.string.input_inline_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            supportingText = {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(R.string.platform_fallback_note),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        )
                        FilledTonalButton(
                            onClick = { viewModel.translate(inputText) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = androidx.compose.ui.res.stringResource(R.string.confirm_input))
                        }
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.translation_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text =
                                when {
                                    uiState.errorMessage != null -> uiState.errorMessage!!
                                    uiState.lastTranslation.isBlank() -> androidx.compose.ui.res.stringResource(R.string.empty_translation)
                                    else -> uiState.lastTranslation
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                if (uiState.errorMessage != null) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                        Text(
                            text =
                                "${androidx.compose.ui.res.stringResource(R.string.detected_source)}: ${languageLabel(uiState.detectedLanguage ?: uiState.sourceLanguage)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                AndroidLanguageSwitcher(
                    sourceLanguage = uiState.sourceLanguage,
                    targetLanguage = uiState.targetLanguage,
                    onSourceClick = viewModel::toggleSourceLanguage,
                    onTargetClick = viewModel::toggleTargetLanguage,
                    onSwapClick = viewModel::swapLanguages,
                )
            }
            item {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.history_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            if (uiState.history.isEmpty()) {
                item {
                    AndroidHistoryCard(
                        item =
                            TranslationHistoryItem(
                                id = "empty",
                                original = androidx.compose.ui.res.stringResource(R.string.empty_history),
                                translated = androidx.compose.ui.res.stringResource(R.string.input_inline_hint),
                                from = uiState.sourceLanguage,
                                to = uiState.targetLanguage,
                                createdAt = "--:--",
                            ),
                    )
                }
            } else {
                items(uiState.history, key = { it.id }) { item ->
                    AndroidHistoryCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun AndroidLanguageSwitcher(
    sourceLanguage: String,
    targetLanguage: String,
    onSourceClick: () -> Unit,
    onTargetClick: () -> Unit,
    onSwapClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onSourceClick,
            modifier = Modifier.weight(1f),
        ) {
            Text(text = languageLabel(sourceLanguage))
        }
        FilledTonalButton(onClick = onSwapClick) {
            Text(text = "<->")
        }
        FilledTonalButton(
            onClick = onTargetClick,
            modifier = Modifier.weight(1f),
        ) {
            Text(text = languageLabel(targetLanguage))
        }
    }
}

@Composable
private fun AndroidHistoryCard(item: TranslationHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = item.original,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${languageLabel(item.from)} -> ${languageLabel(item.to)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = item.translated,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.createdAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

