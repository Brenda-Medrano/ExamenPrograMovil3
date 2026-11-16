package com.example.exaprogramolvil3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.exaprogramolvil3.remote.ui.RemoteScreen
import com.example.exaprogramolvil3.tasks.ui.TaskScreen
import com.example.exaprogramolvil3.ui.theme.ExaPrograMolvil3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ExaPrograMolvil3Theme { ExamHome() } }
    }
}

@Composable
private fun ExamHome() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
        NavigationBar {
            NavigationBarItem(
                selected = tab == 0, onClick = { tab = 0 },
                icon = { Text("⚙") }, label = { Text("E50-1") }
            )
            NavigationBarItem(
                selected = tab == 1, onClick = { tab = 1 },
                icon = { Text("✓") }, label = { Text("E50-2") }
            )
        }
    }) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
            when (tab) {
                0 -> RemoteScreen()
                else -> TaskScreen()
            }
        }
    }
}
