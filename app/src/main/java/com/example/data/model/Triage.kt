package com.example.data.model

import java.util.Locale

data class Triage(
    val patientId: String,
    val systolicBP: Int, // ex: 120
    val diastolicBP: Int, // ex: 80
    val temperatureCelsius: Double, // ex: 38.6
    val weightKg: Double, // ex: 65.0
    val heightCm: Double, // ex: 170.0
    val bloodGlucoseMgDl: Int? = null, // Glicemia em jejum/casual
    val heartRateBpm: Int = 78,
    val painScale: Int = 0, // 0 - 10
    val nurseNotes: String = "",
    val recordedBy: String = "Enf. Carlos Benguela",
    val timestamp: Long = System.currentTimeMillis()
) {
    val bloodPressureFormatted: String
        get() = "$systolicBP/$diastolicBP mmHg"

    val bpStatus: BPStatus
        get() = when {
            systolicBP >= 160 || diastolicBP >= 100 -> BPStatus.STAGE_2_HYPERTENSION
            systolicBP >= 140 || diastolicBP >= 90 -> BPStatus.STAGE_1_HYPERTENSION
            systolicBP >= 130 || diastolicBP >= 85 -> BPStatus.PRE_HYPERTENSION
            systolicBP < 90 || diastolicBP < 60 -> BPStatus.HYPOTENSION
            else -> BPStatus.NORMAL
        }

    val isFever: Boolean
        get() = temperatureCelsius >= 37.8

    val bmi: Double
        get() = if (heightCm > 0) {
            val heightM = heightCm / 100.0
            weightKg / (heightM * heightM)
        } else 0.0

    val bmiFormatted: String
        get() = String.format(Locale.US, "%.1f", bmi)
}

enum class BPStatus(val label: String, val isSevere: Boolean) {
    NORMAL("PA Normal", false),
    PRE_HYPERTENSION("Pré-Hipertensão", false),
    STAGE_1_HYPERTENSION("Hipertensão Grau 1", true),
    STAGE_2_HYPERTENSION("Crise Hipertensiva", true),
    HYPOTENSION("Hipotensão", true)
}
