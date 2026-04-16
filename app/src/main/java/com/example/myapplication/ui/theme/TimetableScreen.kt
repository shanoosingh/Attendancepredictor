package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TimetableScreen(data: AppData) {

    var selectedDay by remember { mutableStateOf("MONDAY") }

    val days = listOf(
        "MONDAY", "TUESDAY", "WEDNESDAY",
        "THURSDAY", "FRIDAY"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("📅 Timetable", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        // 🔥 DAY SELECTOR
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            days.forEach { day ->
                Button(
                    onClick = { selectedDay = day },
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (day == selectedDay)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(day.take(3))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        val daySlots = data.timetable.filter {
            it.dayOfWeek == selectedDay
        }

        if (daySlots.isEmpty()) {
            Text("No classes")
        } else {

            LazyColumn {
                items(daySlots) { slot ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("📘 ${slot.subjectName}")
                            Text("⏰ ${slot.startTime} - ${slot.endTime}")
                        }
                    }
                }
            }
        }
    }
}