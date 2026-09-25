package com.example.basecoloreds.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun GradientZone(
    modifier: Modifier = Modifier,
    isTopZone: Boolean,
    onAngleChanged: (dragX: Float, dragY: Float, centerX: Float, centerY: Float) -> Unit
) {
    var zoneWidth by remember { mutableStateOf(0f) }
    var zoneHeight by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Компонент стал полностью прозрачным, он больше не рисует жесткую черную границу!
            .onGloballyPositioned { layoutCoordinates ->
                zoneWidth = layoutCoordinates.size.width.toFloat()
                zoneHeight = layoutCoordinates.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()

                    val centerX = zoneWidth / 2f
                    val centerY = zoneHeight / 2f
                    onAngleChanged(change.position.x, change.position.y, centerX, centerY)
                }
            }
    )
}
