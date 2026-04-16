package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(data: AppData) {

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val start = LocalDate.parse(data.startDate, formatter)
    val today = LocalDate.now()

    var totalClasses = 0
    var attendedClasses = 0.0

    var date = start

    // ✅ FIXED LOOP
    while (!date.isAfter(today)) {

        if (data.activeDays.contains(date.dayOfWeek.name)) {

            val slots = data.timetable.filter {
                it.dayOfWeek == date.dayOfWeek.name
            }

            totalClasses += slots.size

            slots.forEach { slot ->
                val percent = data.subjects.find {
                    it.name.equals(slot.subjectName, true)
                }?.percentage ?: 0.0

                attendedClasses += percent / 100.0
            }
        }

        date = date.plusDays(1)
    }

    val currentPercent =
        if (totalClasses > 0)
            (attendedClasses / totalClasses) * 100
        else 0.0

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var skipStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var skipEndDate by remember { mutableStateOf<LocalDate?>(null) }

    var resultText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text("🤖 Prediction", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        Text("📊 Current: ${"%.2f".format(currentPercent)}%")

        Spacer(Modifier.height(16.dp))

        // 🔥 SKIP TODAY
        val todaySlots = if (data.activeDays.contains(today.dayOfWeek.name)) {
            data.timetable.filter {
                it.dayOfWeek == today.dayOfWeek.name
            }
        } else emptyList()

        val afterSkipTodayTotal = totalClasses + todaySlots.size
        val afterSkipTodayPercent =
            if (afterSkipTodayTotal > 0)
                (attendedClasses / afterSkipTodayTotal) * 100
            else 0.0

        Text("🔥 If you skip TODAY:")
        Text("→ ${"%.2f".format(afterSkipTodayPercent)}%")

        Spacer(Modifier.height(20.dp))

        // 📅 DATE PICKERS
        Text("📅 Skip Range Prediction")

        Button(onClick = { showStartPicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text(skipStartDate?.toString() ?: "Select Start Date")
        }

        Button(onClick = { showEndPicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text(skipEndDate?.toString() ?: "Select End Date")
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {

                if (skipStartDate != null && skipEndDate != null) {

                    var extraClasses = 0
                    var d = skipStartDate!!

                    while (!d.isAfter(skipEndDate)) {

                        if (data.activeDays.contains(d.dayOfWeek.name)) {

                            val slots = data.timetable.filter {
                                it.dayOfWeek == d.dayOfWeek.name
                            }

                            extraClasses += slots.size
                        }

                        d = d.plusDays(1)
                    }

                    val newTotal = totalClasses + extraClasses
                    val newPercent =
                        if (newTotal > 0)
                            (attendedClasses / newTotal) * 100
                        else 0.0

                    resultText = """
Skipped Classes: $extraClasses
New Total: $newTotal
New Attendance: ${"%.2f".format(newPercent)}%
""".trimIndent()

                } else {
                    resultText = "Select both dates"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }

        Spacer(Modifier.height(10.dp))
        Text(resultText)
    }

    // 📅 START PICKER
    if (showStartPicker) {

        val state = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        skipStartDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showStartPicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    // 📅 END PICKER
    if (showEndPicker) {

        val state = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        skipEndDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showEndPicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}