package com.k3h.yoke

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.k3h.yoke.ui.theme.YokeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val storage = remember { Storage(context) }
            val systemDark = isSystemInDarkTheme()
            var darkMode by rememberSaveable { mutableStateOf(storage.savedDarkMode ?: systemDark) }

            // Let the app draw behind the system bars, and match their icons to the app's mode.
            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    // the strip behind the clock is always dark, so keep its icons light
                    statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
                    navigationBarStyle = if (darkMode) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            YokeTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    YokeApp(
                        darkMode = darkMode,
                        onDarkModeChange = {
                            darkMode = it
                            storage.savedDarkMode = it
                        }
                    )
                }
            }
        }
    }
}