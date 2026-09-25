package com.example.basecoloreds.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.basecoloreds.ui.components.GradientZone
import com.example.basecoloreds.ui.viewmodel.GradientViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MainScreen(viewModel: GradientViewModel) {
    val topState by viewModel.topZoneState.collectAsState()
    val bottomState by viewModel.bottomZoneState.collectAsState()

    // 1. Вычисляем углы наклона для векторов градиента (из градусов в радианы)
    val topRadians = Math.toRadians(topState.rotationAngle.toDouble()).toFloat()
    val bottomRadians = Math.toRadians(bottomState.rotationAngle.toDouble()).toFloat()

    // 2. Создаем единый многоточечный градиент на весь экран.
    // Вместо ухода в черный цвет, верхний цвет плавно перетекает в нижний.
    // Смещение Offset позволяет градиенту "крутиться" в зависимости от углов джойстиков.
    val unifiedGradientBrush = Brush.linearGradient(
        colors = listOf(topState.baseColor, bottomState.baseColor),
        start = Offset(
            x = 500f + 300f * cos(topRadians),
            y = 500f + 300f * sin(topRadians)
        ),
        end = Offset(
            x = 500f + 300f * cos(bottomRadians + Math.PI.toFloat()),
            y = 1500f + 300f * sin(bottomRadians + Math.PI.toFloat())
        )
    )

    // Применяем единый плавно сливающийся фон к общему контейнеру Column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(unifiedGradientBrush)
    ) {
        // Верхняя прозрачная интерактивная зона
        GradientZone(
            modifier = Modifier.weight(1f),
            isTopZone = true,
            onAngleChanged = { dragX, dragY, centerX, centerY ->
                viewModel.updateJoystickPosition(true, dragX, dragY, centerX, centerY)
            }
        )

        // Нижняя прозрачная интерактивная зона
        GradientZone(
            modifier = Modifier.weight(1f),
            isTopZone = false,
            onAngleChanged = { dragX, dragY, centerX, centerY ->
                viewModel.updateJoystickPosition(false, dragX, dragY, centerX, centerY)
            }
        )
    }
}
