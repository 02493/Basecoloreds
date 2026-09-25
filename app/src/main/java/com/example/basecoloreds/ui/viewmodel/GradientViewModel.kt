package com.example.basecoloreds.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.basecoloreds.domain.model.JoystickState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.atan2

class GradientViewModel : ViewModel() {

    // Начальные цвета: Верх — зеленый (120°), Низ — красный (0°). Насыщенность максимальная.
    private val _topZoneState = MutableStateFlow(JoystickState(120f, 1f))
    val topZoneState: StateFlow<JoystickState> = _topZoneState.asStateFlow()

    private val _bottomZoneState = MutableStateFlow(JoystickState(0f, 1f))
    val bottomZoneState: StateFlow<JoystickState> = _bottomZoneState.asStateFlow()

    // Глобальные параметры градиента
    private val _gradientAngle = MutableStateFlow(90f) // Начальный горизонтальный раздел (угол 90)
    val gradientAngle: StateFlow<Float> = _gradientAngle.asStateFlow()

    // Смещение центральной точки по осям X и Y относительно центра экрана
    private val _centerXOffset = MutableStateFlow(0f)
    val centerXOffset: StateFlow<Float> = _centerXOffset.asStateFlow()

    private val _centerYOffset = MutableStateFlow(0f)
    val centerYOffset: StateFlow<Float> = _centerYOffset.asStateFlow()

    // Состояние конечного автомата жестов
    private var currentGestureMode = "NONE"
    // Запоминаем, в какой зоне начался линейный жест (true — верх, false — низ)
    private var initialZoneIsTop = true

    fun resetGesture() {
        currentGestureMode = "NONE"
    }

    /**
     * Точка входа для обработки любого движения пальца
     */
    fun handleMovement(
        currentX: Float,
        currentY: Float,
        prevX: Float,
        prevY: Float,
        screenWidth: Float,
        screenHeight: Float
    ) {
        val dx = currentX - prevX
        val dy = currentY - prevY

        // Фильтр QA: Игнорируем микро-смещения (тапы)
        if (abs(dx) < 1f && abs(dy) < 1f) return

        val halfHeight = screenHeight / 2f
        val currentZoneIsTop = currentY < halfHeight

        // Определяем центр текущей полусферы для расчета углов вращения
        val sphereCenterX = screenWidth / 2f
        val sphereCenterY = if (currentZoneIsTop) screenHeight / 4f else screenHeight * 3f / 4f

        val oldAngle = Math.toDegrees(atan2((prevY - sphereCenterY).toDouble(), (prevX - sphereCenterX).toDouble())).toFloat()
        val newAngle = Math.toDegrees(atan2((currentY - sphereCenterY).toDouble(), (currentX - sphereCenterX).toDouble())).toFloat()
        var deltaAngle = newAngle - oldAngle
        if (deltaAngle > 180f) deltaAngle -= 360f
        if (deltaAngle < -180f) deltaAngle += 360f

        // 1. Инициализация режима при первом движении
        if (currentGestureMode == "NONE") {
            initialZoneIsTop = currentZoneIsTop

            val absDx = abs(dx)
            val absDy = abs(dy)
            val absDeltaAngle = abs(deltaAngle)

            currentGestureMode = when {
                // Если есть явное вращение вокруг центра полусферы
                absDeltaAngle > 1.5f && absDx > 1f && absDy > 1f -> "ROTATE"
                // Строгий вертикальный вектор
                absDy > absDx * 2.5f -> "VERTICAL"
                // Строгий горизонтальный вектор
                absDx > absDy * 2.5f -> "HORIZONTAL"
                // Диагональное смещение
                absDx > 0.8f * absDy && absDx < 1.2f * absDy -> "DIAGONAL"
                else -> "NONE"
            }
        }

        // 2. Выполнение зафиксированного режима
        when (currentGestureMode) {
            "ROTATE" -> {
                // Режим вращения поддерживает смену зон "на лету" (восьмерка)
                val targetFlow = if (currentZoneIsTop) _topZoneState else _bottomZoneState
                var nextHue = (targetFlow.value.hue + deltaAngle) % 360f
                if (nextHue < 0) nextHue += 360f
                targetFlow.value = targetFlow.value.copy(hue = nextHue)
            }

            "VERTICAL" -> {
                // Работает строго с той зоной, где начался жест. Другие изменения заблокированы.
                val targetFlow = if (initialZoneIsTop) _topZoneState else _bottomZoneState
                // Свайп вверх (dy < 0) -> размывает в пастель. Свайп вниз (dy > 0) -> делает сочным.
                val step = 0.003f
                val nextSaturation = (targetFlow.value.saturation + (dy * step)).coerceIn(0.25f, 1.0f)
                targetFlow.value = targetFlow.value.copy(saturation = nextSaturation)
            }

            "HORIZONTAL" -> {
                // Вращает линию градиента вокруг центра экрана. Свайп влево (dx < 0) -> по часовой.
                val angleStep = 0.3f
                var nextAngle = (_gradientAngle.value - (dx * angleStep)) % 360f
                if (nextAngle < 0) nextAngle += 360f
                _gradientAngle.value = nextAngle
            }

            "DIAGONAL" -> {
                // Смещает центральную точку градиента. Ограничиваем движение радиусом в 250 пикселей.
                val maxRadius = 250f
                val potentialX = _centerXOffset.value + dx * 0.8f
                val potentialY = _centerYOffset.value + dy * 0.8f

                val distance = kotlin.math.sqrt(potentialX * potentialX + potentialY * potentialY)
                if (distance <= maxRadius) {
                    _centerXOffset.value = potentialX
                    _centerYOffset.value = potentialY
                }
            }
        }
    }
}
