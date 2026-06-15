package com.klodit.almizan.ui.theme



enum class AppLanguage(
    val label  : String,   // display label shown in the switcher
    val locale : String    // BCP-47 locale tag
) {
    FRENCH ("FRANÇAIS",  "fr"),
    ARABIC ("العربية",   "ar"),
    ENGLISH("ENGLISH",   "en")
}