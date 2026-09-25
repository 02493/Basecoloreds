package com.example.basecoloreds.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Класс данных, описывающий состояние одной управляемой зоны экрана.
 *
 * @property baseColor Текущий базовый цвет половины экрана, полученный в результате вращения.
 * @property rotationAngle Текущий угол поворота виртуального джойстика в градусах (от 0 до 360).
 */
data class JoystickState(
    val baseColor: Color,
    val rotationAngle: Float
)
