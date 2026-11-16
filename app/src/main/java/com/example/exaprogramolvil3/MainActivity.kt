package com.example.exaprogramolvil3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.exaprogramolvil3.remote.ui.RemoteScreen
import com.example.exaprogramolvil3.ui.theme.ExaPrograMolvil3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ExaPrograMolvil3Theme { RemoteScreen() } }
    }
}
