package work.czzzz.wristtrans.ui.model

fun languageLabel(code: String): String =
    when (code.lowercase()) {
        "auto" -> "AUTO"
        "en" -> "EN"
        "zh-cn", "zh" -> "ZH"
        else -> code.uppercase()
    }
