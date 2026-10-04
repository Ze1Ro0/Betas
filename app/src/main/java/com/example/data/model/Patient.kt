package com.example.data.model

enum class PatientStatus(val displayName: String) {
    WAITING_TRIAGE("Aguardando Triagem"),
    WAITING_CONSULTATION("Aguardando Consulta"),
    IN_CONSULTATION("Em Atendimento"),
    COMPLETED("Concluído")
}

data class Patient(
    val id: String,
    val name: String,
    val biNumber: String, // Bilhete de Identidade Angolano
    val phone: String, // +244 ...
    val age: Int,
    val gender: String, // Masculino / Feminino
    val coverageType: String, // Particular, ENSA Seguros, etc.
    val status: PatientStatus,
    val triage: Triage? = null,
    val chiefComplaint: String = "",
    val doctorNotes: String = "",
    val diagnosis: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
