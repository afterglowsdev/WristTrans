package work.czzzz.wristtrans.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme as WearMaterialTheme
import androidx.wear.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// 自定义颜色方案
private val wristTransDarkColorScheme = darkColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = Color(0xFFBB86FC),
    
    secondary = Color(0xFF03DAC5),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF018786),
    onSecondaryContainer = Color(0xFFA7FFEB),
    
    surfaceContainer = Color(0xFF2A2A2A),
    onSurface = Color(0xFFE1E1E1),
    onSurfaceVariant = Color(0xFFC5C5C5),
    
    outline = Color(0xFF8C8C8C),
    outlineVariant = Color(0xFF605E5E),
    
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF7B0014),
    onErrorContainer = Color(0xFFFFB4AB)
)

@Composable
fun WristTransTheme(
    content: @Composable () -> Unit
) {
    WearMaterialTheme(
        colorScheme = wristTransDarkColorScheme,
        shapes = androidx.wear.compose.material3.Shapes(),
        typography = androidx.wear.compose.material3.Typography(),
        content = content
    )
}
