package com.whitestone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.whitestone.app.ui.navigation.WhiteStoneNavGraph
import com.whitestone.app.ui.theme.WhiteStoneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhiteStoneTheme {
                WhiteStoneNavGraph()
            }
        }
    }
}
