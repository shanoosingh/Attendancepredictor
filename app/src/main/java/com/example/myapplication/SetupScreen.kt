package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onSetupDone: (AppData) -> Unit
) {

    var startDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    var subjectName by remember { mutableStateOf("") }
    var subjectPercent by remember { mutableStateOf("") }

    val subjects = remember { mutableStateListOf<SubjectEntity>() }
    val timetable = remember { mutableStateListOf<TimetableEntity>() }

    // ✅ CLASS DAYS
    val selectedDays = remember {
        mutableStateMapOf(
            "MONDAY" to false,
            "TUESDAY" to false,
            "WEDNESDAY" to false,
            "THURSDAY" to false,
            "FRIDAY" to false,
            "SATURDAY" to false
        )
    }

    // 🆕 TIMETABLE INPUT
    var selectedDay by remember { mutableStateOf("MONDAY") }
    var slotSubject by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text("⚙️ Setup", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        // 📅 DATE PICKER
        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (startDate.isEmpty()) "Select Start Date" else startDate)
        }

        Spacer(Modifier.height(16.dp))

        // 📅 CLASS DAYS
        Text("📅 Select Class Days")

        selectedDays.forEach { (day, checked) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { selectedDays[day] = it }
                )
                Text(day)
            }
        }

        Spacer(Modifier.height(16.dp))

        // 📘 SUBJECTS
        Text("📘 Add Subject")

        OutlinedTextField(
            value = subjectName,
            onValueChange = { subjectName = it },
            label = { Text("Subject Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = subjectPercent,
            onValueChange = { subjectPercent = it },
            label = { Text("Attendance %") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val percent = subjectPercent.toDoubleOrNull()
                if (subjectName.isNotBlank() && percent != null) {
                    subjects.add(SubjectEntity(name=subjectName, percentage = percent))
                    subjectName = ""
                    subjectPercent = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Subject")
        }

        Spacer(Modifier.height(16.dp))

        Text("📊 Subjects")

        subjects.forEach {
            Text("${it.name} → ${it.percentage}%")
        }

        Spacer(Modifier.height(20.dp))

        // 🆕 MANUAL TIMETABLE
        Text("📅 Add Timetable")

        Row {
            listOf("MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY").forEach {
                Button(onClick = { selectedDay = it }) {
                    Text(it.take(3))
                }
            }
        }

        OutlinedTextField(
            value = slotSubject,
            onValueChange = { slotSubject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("Start Time") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = endTime,
            onValueChange = { endTime = it },
            label = { Text("End Time") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (slotSubject.isNotBlank()) {
                    timetable.add(
                        TimetableEntity(
                            selectedDay,
                            slotSubject,
                            startTime,
                            endTime
                        )
                    )
                    slotSubject = ""
                    startTime = ""
                    endTime = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Slot")
        }

        Spacer(Modifier.height(12.dp))

        timetable.forEach {
            Text("${it.dayOfWeek} → ${it.subjectName} (${it.startTime})")
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                onSetupDone(
                    AppData(
                        startDate = startDate,
                        subjects = subjects.toList(),
                        timetable = timetable.toList(),
                        activeDays = selectedDays.filter { it.value }.keys.toList()
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Complete Setup")
        }
    }

    // 📅 DATE PICKER
    if (showDatePicker) {

        val state = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {

                    state.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        startDate = date.format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        )
                    }

                    showDatePicker = false
                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}