package com.example.basecoloreds.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import com.example.basecoloreds.ui.viewmodel.GradientViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MainScreen(viewModel: GradientViewModel) {
    val topState by viewModel.topZoneState.collectAsState()
    val bottomState by viewModel.bottomZoneState.collectAsState()
    val gradientAngle by viewModel.gradientAngle.collectAsState()
    val offsetX by viewModel.centerXOffset.collectAsState()
    val offsetY by viewModel.centerYOffset.collectAsState()

    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }

    // Конвертируем цвета в HSV. Черный цвет полностью исключен.
    val topColor = Color.hsv(topState.hue, topState.saturation, 1.0f)
    val bottomColor = Color.hsv(bottomState.hue, bottomState.saturation, 1.0f)

    // Переводим текущий угол вращения в радианы
    val angleRad = Math.toRadians(gradientAngle.toDouble()).toFloat()

    // Динамический центр нашей разделительной полосы градиента
    val dynamicCenterX = (screenWidth / 2f) + offsetX
    val dynamicCenterY = (screenHeight / 2f) + offsetY

    // Координаты направления градиента (строго перпендикулярно невидимой линии раздела)
    val gradientAngleRad = angleRad + (Math.PI.toFloat() / 2f)
    val gradientLength = screenHeight / 2f

    val gradientStart = Offset(
        x = dynamicCenterX - gradientLength * cos(gradientAngleRad),
        y = dynamicCenterY - gradientLength * sin(gradientAngleRad)
    )
    val gradientEnd = Offset(
        x = dynamicCenterX + gradientLength * cos(gradientAngleRad),
        y = dynamicCenterY + gradientLength * sin(gradientAngleRad)
    )

    // Отрисовываем единый бесшовный градиент на весь экран
    val unifiedGradientBrush = Brush.linearGradient(
        colors = listOf(topColor, bottomColor),
        start = gradientStart,
        end = gradientEnd
    )

    var prevX by remember { mutableStateOf(0f) }
    var prevY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(unifiedGradientBrush)
            .onGloballyPositioned { layoutCoordinates ->
                screenWidth = layoutCoordinates.size.width.toFloat()
                screenHeight = layoutCoordinates.size.height.toFloat()
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pointerChange = event.changes.firstOrNull()

                        if (pointerChange != null) {
                            val currentX = pointerChange.position.x
                            val currentY = pointerChange.position.y

                            if (pointerChange.pressed) {
                                pointerChange.consume()

                                if (!isDragging) {
                                    prevX = currentX
                                    prevY = currentY
                                    isDragging = true
                                } else {
                                    viewModel.handleMovement(
                                        currentX = currentX,
                                        currentY = currentY,
                                        prevX = prevX,
                                        prevY = prevY,
                                        screenWidth = screenWidth,
                                        screenHeight = screenHeight
                                    )
                                    prevX = currentX
                                    prevY = currentY
                                }
                            } else {
                                isDragging = false
                                viewModel.resetGesture()
                            }
                        }
                    }
                }
            }
    )
}
