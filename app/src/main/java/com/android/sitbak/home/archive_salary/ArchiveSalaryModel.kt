package com.android.sitbak.home.archive_salary

data class ArchiveSalaryModel(
    val `data`: List<ArchiveSalaryData>,
    val message: String,
    val status: Boolean
)

data class ArchiveSalaryData(
    val date: String,
    val id: Int,
    val money: String
)