package work.czzzz.wristtrans

import android.app.RemoteInput
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.OutlinedCard
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.input.RemoteInputIntentHelper
import work.czzzz.wristtrans.ui.WristTransTheme
import work.czzzz.wristtrans.ui.WristTransViewModel
import work.czzzz.wristtrans.ui.model.TranslationHistoryItem

private const val RemoteInputKey = "translation_text"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WristTransTheme {
                WristTransRoute()
            }
        }
    }
}

@Composable
private fun WristTransRoute(
    viewModel: WristTransViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()
    val inputHint = stringResource(R.string.input_hint)
    val inputTitle = stringResource(R.string.input_title)
    val confirmLabel = stringResource(R.string.confirm_input)
    val cancelLabel = stringResource(R.string.cancel_input)
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val text =
                result.data
                    ?.let(RemoteInput::getResultsFromIntent)
                    ?.getCharSequence(RemoteInputKey)
                    ?.toString()
                    ?.trim()
                    .orEmpty()
            if (text.isNotBlank()) {
                viewModel.translate(text)
            }
        }

    val openInput = {
        val remoteInput =
            RemoteInput.Builder(RemoteInputKey)
                .setLabel(inputHint)
                .build()
        val intent =
            RemoteInputIntentHelper.createActionRemoteInputIntent().apply {
                RemoteInputIntentHelper.putRemoteInputsExtra(this, listOf(remoteInput))
                RemoteInputIntentHelper.putTitleExtra(this, inputTitle)
                RemoteInputIntentHelper.putConfirmLabelExtra(this, confirmLabel)
                RemoteInputIntentHelper.putCancelLabelExtra(this, cancelLabel)
            }
        launcher.launch(intent)
    }

    AppScaffold {
        ScreenScaffold(scrollState = listState) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding =
                    PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 20.dp,
                        bottom = 28.dp,
                    ),
            ) {
                item {
                    InputCard(
                        uiState = uiState,
                        onClick = openInput,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    LanguageSwitcher(
                        sourceLanguage = uiState.sourceLanguage,
                        targetLanguage = uiState.targetLanguage,
                        onSourceClick = viewModel::toggleSourceLanguage,
                        onTargetClick = viewModel::toggleTargetLanguage,
                        onSwapClick = viewModel::swapLanguages,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    ListHeader(modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.history_label))
                    }
                }
                if (uiState.history.isEmpty()) {
                    item {
                        HistoryCard(
                            item =
                                TranslationHistoryItem(
                                    id = "empty",
                                    original = stringResource(R.string.empty_history),
                                    translated = stringResource(R.string.input_hint),
                                    from = uiState.sourceLanguage,
                                    to = uiState.targetLanguage,
                                    createdAt = "--:--",
                                ),
                        )
                    }
                } else {
                    items(uiState.history, key = { it.id }) { item ->
                        HistoryCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun InputCard(
    uiState: work.czzzz.wristtrans.ui.model.TranslationUiState,
    onClick: () -> Unit,
) {
    OutlinedCard(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(),
    ) {
        Text(
            text = stringResource(R.string.input_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (uiState.lastInput.isBlank()) stringResource(R.string.input_hint) else uiState.lastInput,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.translation_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text =
                when {
                    uiState.errorMessage != null -> uiState.errorMessage
                    uiState.lastTranslation.isBlank() -> stringResource(R.string.empty_translation)
                    else -> uiState.lastTranslation
                },
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (uiState.errorMessage != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text =
                "${stringResource(R.string.detected_source)}: ${languageLabel(uiState.detectedLanguage ?: uiState.sourceLanguage)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LanguageSwitcher(
    sourceLanguage: String,
    targetLanguage: String,
    onSourceClick: () -> Unit,
    onTargetClick: () -> Unit,
    onSwapClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onSourceClick,
            label = {
                Text(
                    text = languageLabel(sourceLanguage),
                    style = MaterialTheme.typography.labelSmall,
                )
            },
            secondaryLabel = {
                Text(
                    text = stringResource(R.string.toggle_source),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onSwapClick,
            label = {
                Text(
                    text = "<->",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            modifier = Modifier.weight(1f),
            onClick = onTargetClick,
            label = {
                Text(
                    text = languageLabel(targetLanguage),
                    style = MaterialTheme.typography.labelSmall,
                )
            },
            secondaryLabel = {
                Text(
                    text = stringResource(R.string.toggle_target),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
    }
}

@Composable
private fun HistoryCard(item: TranslationHistoryItem) {
    TitleCard(
        onClick = {},
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        title = {
            Text(
                text = item.original,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        subtitle = {
            Text(
                text = "${languageLabel(item.from)} -> ${languageLabel(item.to)}",
                style = MaterialTheme.typography.labelSmall,
            )
        },
        time = {
            Text(
                text = item.createdAt,
                style = MaterialTheme.typography.labelSmall,
            )
        },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
    ) {
        Text(
            text = item.translated,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun languageLabel(code: String): String =
    when (code.lowercase()) {
        "auto" -> "AUTO"
        "en" -> "EN"
        "zh-cn", "zh" -> "ZH"
        else -> code.uppercase()
    }
