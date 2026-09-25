package com.example.basecoloreds

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.basecoloreds.ui.screens.MainScreen
import com.example.basecoloreds.ui.viewmodel.GradientViewModel

/**
 * Точка входа в Android-приложение.
 * Инициализирует архитектурные компоненты и задает корневой Compose-интерфейс.
 */
class MainActivity : ComponentActivity() {

    // Инициализируем ViewModel через делегат by viewModels().
    // Это гарантирует, что жизненный цикл ViewModel будет привязан к этой Activity,
    // и данные не будут уничтожаться или сбрасываться при повороте экрана смартфона.
    private val gradientViewModel: GradientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent — это мост, который заменяет старую разметку XML на Jetpack Compose
        setContent {
            // Запускаем наш главный экран и передаем туда «мозг» приложения
            MainScreen(viewModel = gradientViewModel)
        }
    }
}
