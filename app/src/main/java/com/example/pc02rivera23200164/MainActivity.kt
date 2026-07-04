package com.example.pc02rivera23200164

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pc02rivera23200164.ui.screens.ConversionScreen
import com.example.pc02rivera23200164.ui.screens.HistoryScreen
import com.example.pc02rivera23200164.ui.screens.LoginScreen
import com.example.pc02rivera23200164.ui.theme.Pc02Rivera23200164Theme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pc02Rivera23200164Theme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) "conversion" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("conversion") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        composable("conversion") {
            ConversionScreen(
                onLogout = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("conversion") { inclusive = true }
                    }
                },
                onGoToHistory = {
                    navController.navigate("history")
                }
            )
        }
        composable("history") {
            HistoryScreen(onBack = {
                navController.popBackStack()
            })
        }
    }
}
