package com.example.myapplication

data class AppData(
    val startDate: String,
    val subjects: List<SubjectEntity>,
    val timetable: List<TimetableEntity>,
    val activeDays: List<String>
)