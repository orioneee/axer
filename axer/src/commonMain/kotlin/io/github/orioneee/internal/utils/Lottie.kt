package io.github.orioneee.internal.utils

import androidx.compose.ui.graphics.Color

private fun List<Double>.applyColorTransformation(
    transform: (Color) -> Color
): List<Double> {
    if (size != 4) return this // Not a color array
    val color = Color(this[0].toFloat(), this[1].toFloat(), this[2].toFloat(), this.getOrNull(3)?.toFloat() ?: 1f)
    val transformedColor = transform(color)

    return listOf(
        transformedColor.red.toDouble(),
        transformedColor.green.toDouble(),
        transformedColor.blue.toDouble(),
        transformedColor.alpha.toDouble()
    )
}


internal fun String.replaceColorsInLottieJson(
    transform: (Color) -> Color
): String {
    val regex =
        """\[\s*(-?\d+(?:\.\d+)?(?:e[+-]?\d+)?\s*,\s*){2,3}-?\d+(?:\.\d+)?(?:e[+-]?\d+)?\s*]""".toRegex(
            RegexOption.IGNORE_CASE
        )


    return regex.replace(this) { matchResult ->
        val original = matchResult.value

        val numbers = original
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")
            .map { it.trim().toDouble() }

        val newNumbers = numbers.applyColorTransformation(transform)

        newNumbers.joinToString(prefix = "[", postfix = "]") { it.toString() }
    }
}

internal fun String.getUniqueColorsInLottieJson(): List<Color> {
    val regex =
        "\\[\\s*(-?\\d+(?:\\.\\d+)?(?:e[+-]?\\d+)?\\s*,\\s*){2,3}-?\\d+(?:\\.\\d+)?(?:e[+-]?\\d+)?\\s*]\n".toRegex(
            RegexOption.IGNORE_CASE
        )

    return regex.findAll(this)
        .map { matchResult ->
            val numbers = matchResult.value
                .removePrefix("[")
                .removeSuffix("]")
                .split(",")
                .map { it.trim().toDouble() }
            Color(
                numbers[0].toFloat(),
                numbers[1].toFloat(),
                numbers[2].toFloat(),
                numbers[3].toFloat()
            )
        }
        .distinct()
        .toList()
}