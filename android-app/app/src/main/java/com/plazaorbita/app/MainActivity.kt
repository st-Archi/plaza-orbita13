package com.plazaorbita.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.plazaorbita.app.data.remote.RetrofitClient
import com.plazaorbita.app.ui.navigation.PlazaOrbitaNavGraph
import com.plazaorbita.app.ui.theme.PlazaOrbitaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(applicationContext)
        setContent {
            PlazaOrbitaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    PlazaOrbitaNavGraph(navController = navController)
                }
            }
        }
    }
}