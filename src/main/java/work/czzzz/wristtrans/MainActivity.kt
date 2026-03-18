package work.czzzz.wristtrans

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.ScalingLazyColumn
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Chip
import androidx.wear.compose.material3.ChipDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.TitleCard
import work.czzzz.wristtrans.network.NetworkModule
import work.czzzz.wristtrans.ui.WristTransTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var updateInputText: (String) -> Unit
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WristTransTheme {
                WristTransApp {
                    updateInputText = it
                    launchRemoteInput()
                }
            }
        }
    }
    
    private fun launchRemoteInput() {
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        val remoteInputs = listOf(
            androidx.wear.input.RemoteInput.Builder("input")
                .setLabel(getString(R.string.input_hint))
                .wearableExtender { }
                .build()
        )
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
        startActivityForResult(intent, 1)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val input = androidx.wear.input.RemoteInput.getResultsFromIntent(data)?.getCharSequence("input")
            input?.let {
                updateInputText(it.toString())
            }
        }
    }
}

@Composable
fun WristTransApp(onInputClick: ((String) -> Unit) -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    var sourceLanguage by remember { mutableStateOf("EN") }
    var targetLanguage by remember { mutableStateOf("ZH") }
    var history by remember { mutableStateOf(listOf<String>()) }

    fun translateText(text: String) {
        if (text.isBlank()) return
        
        isTranslating = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = NetworkModule.translationService
                    .translate(
                        query = text,
                        to = if (targetLanguage == "ZH") "zh-CN" else targetLanguage.lowercase(),
                        from = "auto"
                    )
                    .execute()
                
                if (response.isSuccessful) {
                    val translation = response.body()
                    if (translation != null) {
                        outputText = translation.translated
                        history = listOf("$text → ${translation.translated}") + history.take(9)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isTranslating = false
            }
        }
    }

    AppScaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
        ) {
            // 输入区域 - 大号 MD3 OutlinedCard
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 4.dp),
                    onClick = { 
                        onInputClick { newText -> 
                            inputText = newText
                            translateText(newText)
                        }
                    },
                    shape = MaterialTheme.shapes.large
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 输入文字
                            Text(
                                text = if (inputText.isEmpty()) "点击输入..." else inputText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = if (inputText.isEmpty()) FontWeight.Normal else FontWeight.SemiBold
                                ),
                                textAlign = TextAlign.Center,
                                color = if (inputText.isEmpty()) 
                                    MaterialTheme.colorScheme.onSurfaceVariant 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                            
                            // 翻译结果
                            if (outputText.isNotEmpty()) {
                                Text(
                                    text = "→",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = outputText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // 加载动画
                            if (isTranslating) {
                                Spacer(modifier = Modifier.size(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    trackColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            }
                        }
                    }
                }
            }

            // 语言切换区域 - 圆润 Chip 双语言显示
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Chip(
                        onClick = { 
                            // 交换语言和文本
                            val tempLang = sourceLanguage
                            sourceLanguage = targetLanguage
                            targetLanguage = tempLang
                            
                            val tempText = inputText
                            inputText = outputText
                            outputText = tempText
                            
                            if (inputText.isNotEmpty()) {
                                translateText(inputText)
                            }
                        },
                        label = { 
                            Text(
                                text = "$sourceLanguage ↔ $targetLanguage",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp
                                )
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .padding(horizontal = 8.dp),
                        colors = ChipDefaults.chipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = ChipDefaults.outlinedChipBorder(),
                        shape = MaterialTheme.shapes.extraLarge
                    )
                }
            }

            // 历史记录标签
            if (history.isNotEmpty()) {
                item {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
            }

            // 历史记录列表 - TitleCard 样式
            items(history.take(5)) { entry ->
                TitleCard(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 3.dp),
                    title = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { 
                                    history = history.filter { it != entry }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        val parts = entry.split(" → ")
                        if (parts.size == 2) {
                            inputText = parts[0]
                            outputText = parts[1]
                        }
                    }
                )
            }
            
            // 空状态提示
            if (history.isEmpty() && inputText.isEmpty()) {
                item {
                    Text(
                        text = "No translation history",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            }
        }
    }
}
