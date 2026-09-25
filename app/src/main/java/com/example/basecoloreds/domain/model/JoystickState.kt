package com.example.basecoloreds.domain.model

/**
 * @param hue Оттенок цвета (0..360)
 * @param saturation Насыщенность/пастельность (0.25..1.0). Исключает белый цвет.
 */
data class JoystickState(
    val hue: Float,
    val saturation: Float
)
