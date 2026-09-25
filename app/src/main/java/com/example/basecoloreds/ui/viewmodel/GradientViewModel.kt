package com.example.basecoloreds.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.basecoloreds.domain.model.JoystickState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2

/**
 * Архитектурный компонент ViewModel для управления состоянием градиентов половин экрана.
 * Отвечает за независимую обработку жестов и реактивное обновление цветов.
 */
class GradientViewModel : ViewModel() {

    // Начальный базовый цвет для обеих зон — глубокий темно-синий.
    // При старте обе половины имеют одинаковый цвет, создавая сплошной однотонный фон.
    private val defaultColor = Color(0xFF001F3F)

    // Внутреннее изменяемое состояние верхней половины экрана
    private val _topZoneState = MutableStateFlow(JoystickState(defaultColor, 0f))
    // Открытое состояние для чтения интерфейсом (ReadOnly)
    val topZoneState: StateFlow<JoystickState> = _topZoneState.asStateFlow()

    // Внутреннее изменяемое состояние нижней половины экрана
    private val _bottomZoneState = MutableStateFlow(JoystickState(defaultColor, 0f))
    // Открытое состояние для чтения интерфейсом (ReadOnly)
    val bottomZoneState: StateFlow<JoystickState> = _bottomZoneState.asStateFlow()

    /**
     * Вычисляет угол движения пальца относительно центра зоны и трансформирует его в цвет.
     *
     * @param isTopZone Флаг, определяющий какую половину экрана обновлять (true — верх, false — низ).
     * @param dragX Текущая координата X пальца пользователя на экране.
     * @param dragY Текущая координата Y пальца пользователя на экране.
     * @param centerX Координата X геометрического центра этой половины экрана.
     * @param centerY Координата Y геометрического центра этой половины экрана.
     */
    fun updateJoystickPosition(
        isTopZone: Boolean,
        dragX: Float,
        dragY: Float,
        centerX: Float,
        centerY: Float
    ) {
        // 1. Вычисляем вектор смещения (расстояние от центра сферы до пальца)
        val deltaX = dragX - centerX
        val deltaY = dragY - centerY

        // 2. Находим угол в радианах с помощью тригонометрического арктангенса
        val radians = atan2(deltaY, deltaX)

        // 3. Переводим радианы в градусы (от -180 до 180)
        var degrees = Math.toDegrees(radians.toDouble()).toFloat()

        // 4. Нормализуем угол, чтобы значения были строго в диапазоне от 0 до 360 градусов
        if (degrees < 0) {
            degrees += 360f
        }

        // 5. Генерируем цвет на основе полученного угла через модель HSV.
        // Параметр Hue (оттенок) принимает значения от 0 до 360, идеально ложась на наш круг.
        val generatedColor = Color.hsv(
            hue = degrees,
            saturation = 0.85f,
            value = 0.9f
        )

        // 6. Атомарно обновляем состояние нужной половины экрана через метод copy()
        if (isTopZone) {
            _topZoneState.value = _topZoneState.value.copy(
                baseColor = generatedColor,
                rotationAngle = degrees
            )
        } else {
            _bottomZoneState.value = _bottomZoneState.value.copy(
                baseColor = generatedColor,
                rotationAngle = degrees
            )
        }
    }
}
