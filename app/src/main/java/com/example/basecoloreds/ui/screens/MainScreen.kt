package com.example.basecoloreds.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.basecoloreds.ui.components.GradientZone
import com.example.basecoloreds.ui.viewmodel.GradientViewModel

/**
 * Главный экран приложения. Разделяет рабочую область на две равные половины
 * и связывает их с бизнес-логикой управления цветами.
 *
 * @param viewModel Экземпляр архитектурного компонента для управления состояниями.
 */
@Composable
fun MainScreen(viewModel: GradientViewModel) {
    // Реактивно подписываемся на изменения состояний обеих зон из ViewModel.
    // При изменении StateFlow, Compose автоматически перерисует только нужную зону.
    val topState by viewModel.topZoneState.collectAsState()
    val bottomState by viewModel.bottomZoneState.collectAsState()

    // Размещаем компоненты вертикально друг под другом на весь экран
    Column(modifier = Modifier.fillMaxSize()) {

        // 1. Верхняя независимая половина экрана
        GradientZone(
            state = topState,
            // Modifier.weight(1f) поровну делит пространство экрана между элементами Column
            modifier = Modifier.weight(1f),
            isTopZone = true,
            onAngleChanged = { dragX, dragY, centerX, centerY ->
                // Передаем координаты жеста во ViewModel, указывая флаг верха (true)
                viewModel.updateJoystickPosition(
                    isTopZone = true,
                    dragX = dragX,
                    dragY = dragY,
                    centerX = centerX,
                    centerY = centerY
                )
            }
        )

        // 2. Нижняя независимая половина экрана
        GradientZone(
            state = bottomState,
            modifier = Modifier.weight(1f),
            isTopZone = false,
            onAngleChanged = { dragX, dragY, centerX, centerY ->
                // Передаем координаты жеста во ViewModel, указывая флаг низа (false)
                viewModel.updateJoystickPosition(
                    isTopZone = false,
                    dragX = dragX,
                    dragY = dragY,
                    centerX = centerX,
                    centerY = centerY
                )
            }
        )
    }
}
