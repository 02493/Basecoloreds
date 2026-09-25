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

    // Конвертируем цвета. Значение яркости (Value) всегда 1.0f (черный исключен)
    val topColor = Color.hsv(topState.hue, topState.saturation, 1.0f)
    val bottomColor = Color.hsv(bottomState.hue, bottomState.saturation, 1.0f)

    // Математический расчет точек старта и финиша линии градиента вокруг динамического центра
    val angleRad = Math.toRadians(gradientAngle.toDouble()).toFloat()

    // Базовый геометрический центр экрана плюс смещение от диагональных жестов
    val dynamicCenterX = (screenWidth / 2f) + offsetX
    val dynamicCenterY = (screenHeight / 2f) + offsetY

    // Длина вектора градиента (чтобы полностью перекрывать экран при вращении)
    val vectorLength = screenHeight / 2f

    val startPoint = Offset(
        x = dynamicCenterX - vectorLength * cos(angleRad),
        y = dynamicCenterY - vectorLength * sin(angleRad)
    )
    val endPoint = Offset(
        x = dynamicCenterX + vectorLength * cos(angleRad),
        y = dynamicCenterY + vectorLength * sin(angleRad)
    )

    val unifiedGradientBrush = Brush.linearGradient(
        colors = listOf(topColor, bottomColor),
        start = startPoint,
        end = endPoint
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
                                // Палец оторван — полностью сбрасываем залоченный режим автомата
                                isDragging = false
                                viewModel.resetGesture()
                            }
                        }
                    }
                }
            }
    )
}
