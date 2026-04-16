package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

@Composable
fun DashboardScreen(
    data: AppData,
    onReset: () -> Unit
) {

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val start = LocalDate.parse(data.startDate, formatter)
    val today = LocalDate.now()

    var total = 0
    var attended = 0.0

    var date = start

    while (!date.isAfter(today)) {

        // 🔥 fallback if activeDays not present
        val isActive = try {
            data.activeDays.contains(date.dayOfWeek.name)
        } catch (e: Exception) {
            true
        }

        if (isActive) {

            val slots = data.timetable.filter {
                it.dayOfWeek == date.dayOfWeek.name
            }

            total += slots.size

            slots.forEach { slot ->

                val percent = data.subjects.find {
                    slot.subjectName.lowercase().contains(it.name.lowercase())
                }?.percentage ?: 0.0

                attended += percent / 100.0
            }
        }

        date = date.plusDays(1)
    }

    val percent = if (total > 0) (attended / total) * 100 else 0.0

    val requiredPercent = 75.0
    val requiredClasses = ceil(total * requiredPercent / 100).toInt()

    val safeBunks = (attended - requiredClasses).toInt().coerceAtLeast(0)
    val needToAttend = (requiredClasses - attended).toInt().coerceAtLeast(0)

    // 🔥 TODAY LOGIC
    val todaySlots = data.timetable.filter {
        it.dayOfWeek == today.dayOfWeek.name
    }

    val skipTodayPercent =
        if (total + todaySlots.size > 0)
            (attended / (total + todaySlots.size)) * 100
        else percent

    // 🔥 RANGE SKIP
    var skipStart by remember { mutableStateOf("") }
    var skipEnd by remember { mutableStateOf("") }
    var skipResult by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        item {
            Text("📊 Dashboard", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(10.dp))

            Text("Total Classes: $total")
            Text("Attended Classes: ${attended.toInt()}")
            Text("Overall: ${"%.2f".format(percent)}%")

            Spacer(Modifier.height(12.dp))

            Text("🎯 Required: $requiredPercent%")
            Text("📌 Safe Bunks: $safeBunks")
            Text("📈 Attend next $needToAttend classes to reach 75%")

            Spacer(Modifier.height(12.dp))

            Text("🔥 If you skip TODAY → ${"%.2f".format(skipTodayPercent)}%")

            Spacer(Modifier.height(16.dp))

            Text("📅 Skip Range Prediction")

            OutlinedTextField(
                value = skipStart,
                onValueChange = { skipStart = it },
                label = { Text("Start Date (dd/MM/yyyy)") }
            )

            OutlinedTextField(
                value = skipEnd,
                onValueChange = { skipEnd = it },
                label = { Text("End Date (dd/MM/yyyy)") }
            )

            Button(onClick = {

                try {
                    val s = LocalDate.parse(skipStart, formatter)
                    val e = LocalDate.parse(skipEnd, formatter)

                    var tempDate = s
                    var skipped = 0

                    while (!tempDate.isAfter(e)) {

                        val slots = data.timetable.filter {
                            it.dayOfWeek == tempDate.dayOfWeek.name
                        }

                        skipped += slots.size
                        tempDate = tempDate.plusDays(1)
                    }

                    val newTotal = total + skipped
                    val newPercent =
                        if (newTotal > 0) (attended / newTotal) * 100 else percent

                    skipResult =
                        "Skipped: $skipped\nNew %: ${"%.2f".format(newPercent)}"

                } catch (e: Exception) {
                    skipResult = "Invalid dates"
                }

            }) {
                Text("Calculate")
            }

            Text(skipResult)

            Spacer(Modifier.height(16.dp))

            Text("📘 Subjects")
        }

        items(data.subjects) {
            Text("${it.name} → ${it.percentage}%")
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("📅 Timetable")
        }

        items(data.timetable) {
            Text("${it.dayOfWeek} → ${it.subjectName}")
        }

        item {
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reset App")
            }
        }
    }
}