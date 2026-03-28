package io.github.orioneee.internal.presentation.components

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
internal data class AxerColors(
    val methodGet: Color,
    val methodPost: Color,
    val methodPut: Color,
    val methodDelete: Color,
    val methodPatch: Color,
    val methodDefault: Color,

    val status2xx: Color,
    val status3xx: Color,
    val status4xx: Color,
    val status5xx: Color,
    val statusPending: Color,

    val logVerbose: Color,
    val logDebug: Color,
    val logInfo: Color,
    val logWarning: Color,
    val logError: Color,
    val logAssert: Color,

    val accent: Color,
    val accentDim: Color,
    val cardBorder: Color,
    val cardBorderSelected: Color,
    val codeBackground: Color,
    val warning: Color,
    val success: Color,
)

internal val DarkAxerColors = AxerColors(
    methodGet = Color(0xFF60A5FA),
    methodPost = Color(0xFF34D399),
    methodPut = Color(0xFFFBBF24),
    methodDelete = Color(0xFFF87171),
    methodPatch = Color(0xFFC084FC),
    methodDefault = Color(0xFF94A3B8),

    status2xx = Color(0xFF34D399),
    status3xx = Color(0xFFFBBF24),
    status4xx = Color(0xFFFB923C),
    status5xx = Color(0xFFF87171),
    statusPending = Color(0xFF94A3B8),

    logVerbose = Color(0xFF94A3B8),
    logDebug = Color(0xFF94A3B8),
    logInfo = Color(0xFF60A5FA),
    logWarning = Color(0xFFFBBF24),
    logError = Color(0xFFF87171),
    logAssert = Color(0xFFEF4444),

    accent = Color(0xFF22D3EE),
    accentDim = Color(0x2622D3EE),
    cardBorder = Color(0xFF1E293B),
    cardBorderSelected = Color(0xFF6366F1),
    codeBackground = Color(0xFF0F172A),
    warning = Color(0xFFFBBF24),
    success = Color(0xFF34D399),
)

internal val LightAxerColors = AxerColors(
    methodGet = Color(0xFF2563EB),
    methodPost = Color(0xFF059669),
    methodPut = Color(0xFFD97706),
    methodDelete = Color(0xFFDC2626),
    methodPatch = Color(0xFF9333EA),
    methodDefault = Color(0xFF64748B),

    status2xx = Color(0xFF059669),
    status3xx = Color(0xFFD97706),
    status4xx = Color(0xFFEA580C),
    status5xx = Color(0xFFDC2626),
    statusPending = Color(0xFF64748B),

    logVerbose = Color(0xFF64748B),
    logDebug = Color(0xFF64748B),
    logInfo = Color(0xFF2563EB),
    logWarning = Color(0xFFD97706),
    logError = Color(0xFFDC2626),
    logAssert = Color(0xFFB91C1C),

    accent = Color(0xFF0891B2),
    accentDim = Color(0x260891B2),
    cardBorder = Color(0xFFE2E8F0),
    cardBorderSelected = Color(0xFF4F46E5),
    codeBackground = Color(0xFFF1F5F9),
    warning = Color(0xFFD97706),
    success = Color(0xFF059669),
)

internal val LocalAxerColors = staticCompositionLocalOf { DarkAxerColors }

internal fun AxerColors.methodColor(method: String): Color = when (method.uppercase()) {
    "GET" -> methodGet
    "POST" -> methodPost
    "PUT" -> methodPut
    "DELETE" -> methodDelete
    "PATCH" -> methodPatch
    else -> methodDefault
}

internal fun AxerColors.statusColor(code: Int?): Color = when {
    code == null -> statusPending
    code in 200..299 -> status2xx
    code in 300..399 -> status3xx
    code in 400..499 -> status4xx
    code >= 500 -> status5xx
    else -> statusPending
}

internal val Color.Companion.Warning: Color
    get() = Color(0xFFFBBF24)

internal val ColorScheme.warning: Color
    get() = Color.Warning
