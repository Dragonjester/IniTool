package com.caa.initool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.caa.initool.data.AppDatabase
import com.caa.initool.ui.screen.GegnerListScreen
import com.caa.initool.ui.screen.KampfScreen
import com.caa.initool.ui.screen.PlayerListScreen
import com.caa.initool.ui.screen.SplashScreen
import com.caa.initool.ui.theme.IniToolTheme
import com.caa.initool.viewmodel.GegnerViewModel
import com.caa.initool.viewmodel.GegnerViewModelFactory
import com.caa.initool.viewmodel.PlayerViewModel
import com.caa.initool.viewmodel.PlayerViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getInstance(this)
        val playerFactory = PlayerViewModelFactory(database.playerDao())
        val gegnerFactory = GegnerViewModelFactory(database.encounterDao(), database.gegnerDao())

        enableEdgeToEdge()
        setContent {
            IniToolTheme {
                val navController = rememberNavController()
                val playerViewModel: PlayerViewModel = viewModel(factory = playerFactory)
                val gegnerViewModel: GegnerViewModel = viewModel(factory = gegnerFactory)

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(onTimeout = {
                            navController.navigate("gegner") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }
                    composable("gegner") {
                        GegnerListScreen(
                            viewModel = gegnerViewModel,
                            onNavigateToSpieler = { navController.navigate("spieler") }
                        )
                    }
                    composable("spieler") {
                        PlayerListScreen(
                            viewModel = playerViewModel,
                            onNavigateToKampf = { navController.navigate("kampf") }
                        )
                    }
                    composable("kampf") {
                        KampfScreen(
                            playerViewModel = playerViewModel,
                            gegnerViewModel = gegnerViewModel
                        )
                    }
                }
            }
        }
    }
}
