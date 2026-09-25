package com.example.basecoloreds.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import com.example.basecoloreds.domain.model.JoystickState
import kotlin.math.cos
import kotlin.math.sin

/**
 * Интерактивный UI-компонент половины экрана. Отрисовывает градиент и обрабатывает круговые жесты.
 *
 * @param state Текущее состояние зоны (цвет и угол), полученное из ViewModel.
 * @param modifier Модификатор для настройки размеров и веса (weight) компонента.
 * @param isTopZone Флаг, указывающий, является ли зона верхней (true) или нижней (false).
 * @param onAngleChanged Лямбда-функция, отправляющая координаты жеста и центра сферы во ViewModel.
 */
@Composable
fun GradientZone(
    state: JoystickState,
    modifier: Modifier = Modifier,
    isTopZone: Boolean,
    onAngleChanged: (dragX: Float, dragY: Float, centerX: Float, centerY: Float) -> Unit
) {
    // Храним ширину и высоту этой половины экрана, чтобы точно знать её центр
    var zoneWidth by remember { mutableStateOf(0f) }
    var zoneHeight by remember { mutableStateOf(0f) }

    // Вычисляем координаты центра зоны
    val centerX = zoneWidth / 2f
    val centerY = zoneHeight / 2f

    // Переводим текущий угол поворота из градусов в радианы для тригонометрических расчетов
    val angleInRadians = Math.toRadians(state.rotationAngle.toDouble()).toFloat()

    // Динамически вычисляем конечную точку градиента (его вектор направления),
    // чтобы создать эффект закручивания цвета вслед за пальцем.
    val radius = zoneWidth.coerceAtLeast(zoneHeight) / 2f
    val endX = centerX + radius * cos(angleInRadians)
    val endY = centerY + radius * sin(angleInRadians)

    // Создаем плавный линейный градиент. Он переходит от выбранного пользователем цвета к черному.
    val gradientBrush = Brush.linearGradient(
        colors = listOf(state.baseColor, Color.Black),
        start = Offset(centerX, centerY),
        end = Offset(endX, endY)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            // Накладываем динамический градиент на фон
            .background(gradientBrush)
            // onGloballyPositioned срабатывает при отрисовке интерфейса и отдает реальные размеры зоны в пикселях
            .onGloballyPositioned { layoutCoordinates ->
                zoneWidth = layoutCoordinates.size.width.toFloat()
                zoneHeight = layoutCoordinates.size.height.toFloat()
            }
            // pointerInput — это низкоуровневый перехватчик любых касаний экрана
            .pointerInput(Unit) {
                // detectDragGestures отслеживает непрерывное перемещение пальца (скролл/драг)
                detectDragGestures { change, _ ->
                    // Потребляем событие касания, чтобы операционная система Android не пыталась
                    // интерпретировать этот жест как системный (например, "назад" или вызов шторки)
                    change.consume()

                    // Отправляем текущие координаты пальца (change.position) и центр полусферы
                    // наверх во ViewModel для математического перерасчета цвета
                    onAngleChanged(change.position.x, change.position.y, centerX, centerY)
                }
            }
    )
}
