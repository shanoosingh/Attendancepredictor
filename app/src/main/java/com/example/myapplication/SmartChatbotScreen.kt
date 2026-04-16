package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// 💬 Model
data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun SmartChatbotScreen(data: AppData) {

    var input by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }

    val suggestions = listOf(
        "My attendance",
        "Can I skip today?",
        "Safe bunks?",
        "Subject wise attendance",
        "Weak subject"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Text("🤖 Smart Assistant", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                ChatBubble(msg)
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(suggestions) { text ->
                AssistChip(
                    onClick = {
                        val response = generateSmartReply(text, data)
                        messages.add(ChatMessage(text, true))
                        messages.add(ChatMessage(response, false))
                    },
                    label = { Text(text) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask something...") }
            )

            Spacer(Modifier.width(6.dp))

            Button(onClick = {
                val userText = input.trim()
                if (userText.isBlank()) return@Button

                messages.add(ChatMessage(userText, true))

                val response = generateSmartReply(userText, data)

                messages.add(ChatMessage(response, false))

                input = ""
            }) {
                Text("Send")
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {

    val bubbleColor = if (msg.isUser)
        Color(0xFFDCF8C6)
    else
        Color(0xFFEDEDED)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .background(bubbleColor, RoundedCornerShape(16.dp))
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(msg.text)
        }
    }
}

//////////////////////////////////////////////////////
// 🔥 MAIN AI ENGINE (FIXED)
//////////////////////////////////////////////////////

fun generateSmartReply(query: String, data: AppData): String {

    val cleaned = normalize(query)
    val intents = detectIntents(cleaned)
    val subject = extractSubject(cleaned, data)

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val start = LocalDate.parse(data.startDate, formatter)
    val today = LocalDate.now()

    var total = 0
    var attended = 0.0
    var date = start

    // ✅ FIXED ATTENDANCE LOOP
    while (!date.isAfter(today)) {

        if (data.activeDays.contains(date.dayOfWeek.name)) {

            val slots = data.timetable.filter {
                it.dayOfWeek == date.dayOfWeek.name
            }

            total += slots.size

            slots.forEach { slot ->
                val percent = data.subjects.find {
                    it.name.equals(slot.subjectName, true)
                }?.percentage?: 0.0

                attended += percent / 100
            }
        }

        date = date.plusDays(1)
    }

    val percent = if (total > 0) (attended / total) * 100 else 0.0

    val responses = mutableListOf<String>()

    intents.forEach { intent ->

        when (intent) {

            "attendance" -> {
                if (subject != null) {
                    responses.add("📘 $subject: ${getSubjectPercent(subject, data)}%")
                } else {
                    responses.add("📊 Overall: ${"%.2f".format(percent)}%")
                }
            }

            "safe_bunk" -> {
                val safe = ((percent - 75) / 100 * total).toInt()
                responses.add("📌 Safe bunks: $safe")
            }

            "skip_today" -> {
                val todaySlots = if (data.activeDays.contains(today.dayOfWeek.name)) {
                    data.timetable.filter {
                        it.dayOfWeek == today.dayOfWeek.name
                    }
                } else emptyList()

                val newTotal = total + todaySlots.size
                val newPercent =
                    if (newTotal > 0) (attended / newTotal) * 100 else 0.0

                responses.add("🔥 After skipping today → ${"%.2f".format(newPercent)}%")
            }

            "subject_list" -> {
                val list = data.subjects.joinToString("\n") {
                    "${it.name}: ${it.percentage}%"
                }
                responses.add("📘 Subjects:\n$list")
            }

            "weak_subject" -> {
                val weak = data.subjects.minByOrNull { it.percentage }
                responses.add("⚠️ Weakest: ${weak?.name} (${weak?.percentage}%)")
            }

            "strong_subject" -> {
                val strong = data.subjects.maxByOrNull { it.percentage }
                responses.add("✅ Strongest: ${strong?.name} (${strong?.percentage}%)")
            }

            "need_attend" -> {
                val needed = ((75 * total / 100) - attended).toInt()
                responses.add("📈 Attend $needed classes to reach 75%")
            }
        }
    }

    return if (responses.isNotEmpty()) {
        responses.joinToString("\n\n")
    } else {
        "🤖 Try asking differently"
    }
}

//////////////////////////////////////////////////////
// 🧠 HELPER FUNCTIONS (REQUIRED)
//////////////////////////////////////////////////////

fun normalize(text: String): String {
    return text.lowercase()
        .replace("?", "")
        .replace(",", "")
}

fun detectIntents(q: String): List<String> {

    val intents = mutableListOf<String>()

    if (q.contains("attendance") || q.contains("percent"))
        intents.add("attendance")

    if (q.contains("safe") || q.contains("bunk"))
        intents.add("safe_bunk")

    if (q.contains("skip") && q.contains("today"))
        intents.add("skip_today")

    if (q.contains("subject"))
        intents.add("subject_list")

    if (q.contains("weak") || q.contains("low"))
        intents.add("weak_subject")

    if (q.contains("strong") || q.contains("best"))
        intents.add("strong_subject")

    if (q.contains("how many") || q.contains("reach"))
        intents.add("need_attend")

    return intents
}

fun extractSubject(query: String, data: AppData): String? {
    return data.subjects.find { subject ->
        query.lowercase().contains(subject.name.lowercase())
    }?.name
}

fun getSubjectPercent(name: String, data: AppData): Double {
    return data.subjects.find { subject ->
        subject.name.equals(name, ignoreCase = true)
    }?.percentage ?: 0.0
}