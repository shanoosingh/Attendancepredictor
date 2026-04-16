package com.example.myapplication

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures

@Composable
fun MainScreen() {

    val navController = rememberNavController()

    var isSetupComplete by remember { mutableStateOf(false) }
    var appData by remember { mutableStateOf<AppData?>(null) }

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(

        // 🔻 Bottom Navigation
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = currentRoute == "setup",
                    onClick = { navController.navigate("setup") },
                    label = { Text("Setup") },
                    icon = { Icon(Icons.Default.Settings, null) }
                )

                NavigationBarItem(
                    selected = currentRoute == "dashboard",
                    onClick = {
                        if (isSetupComplete)
                            navController.navigate("dashboard")
                    },
                    label = { Text("Dashboard") },
                    icon = { Icon(Icons.Default.Home, null) }
                )

                NavigationBarItem(
                    selected = currentRoute == "prediction",
                    onClick = {
                        if (isSetupComplete)
                            navController.navigate("prediction")
                    },
                    label = { Text("Predict") },
                    icon = { Icon(Icons.Default.Analytics, null) }
                )

                // 🆕 Timetable Screen
                NavigationBarItem(
                    selected = currentRoute == "timetable",
                    onClick = {
                        if (isSetupComplete)
                            navController.navigate("timetable")
                    },
                    label = { Text("Table") },
                    icon = { Icon(Icons.Default.DateRange, null) }
                )
            }
        },

        // 🤖 Floating AI Button (Draggable)
        floatingActionButton = {

            if (appData != null &&
                currentRoute != "setup" &&
                currentRoute != "chat"
            ) {

                var offsetX by remember { mutableStateOf(0f) }
                var offsetY by remember { mutableStateOf(0f) }

                FloatingActionButton(
                    onClick = { navController.navigate("chat") },
                    modifier = Modifier
                        .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        }
                ) {
                    Icon(Icons.Default.Android, contentDescription = null)
                }
            }
        }

    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "setup",
            modifier = Modifier.padding(padding)
        ) {

            // ⚙️ Setup Screen
            composable("setup") {
                SetupScreen { data ->
                    appData = data
                    isSetupComplete = true
                    navController.navigate("dashboard")
                }
            }

            // 📊 Dashboard
            composable("dashboard") {
                appData?.let {
                    DashboardScreen(it) {
                        appData = null
                        isSetupComplete = false
                        navController.navigate("setup") {
                            popUpTo(0)
                        }
                    }
                } ?: Text("No Data")
            }

            // 📈 Prediction
            composable("prediction") {
                appData?.let {
                    PredictionScreen(it)
                } ?: Text("No Data")
            }

            // 📅 Timetable
            composable("timetable") {
                appData?.let {
                    TimetableScreen(it)
                } ?: Text("No Data")
            }

            // 🤖 Chatbot
            composable("chat") {
                appData?.let {
                    SmartChatbotScreen(it)
                } ?: Text("No Data")
            }
        }
    }
}