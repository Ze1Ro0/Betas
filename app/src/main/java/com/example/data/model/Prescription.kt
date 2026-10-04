package com.example.data.model

enum class PrescriptionStatus(val label: String) {
    PENDING_DISPENSE("Pendente na Farmácia"),
    DISPENSED("Dispensada"),
    CANCELLED("Cancelada")
}

data class PrescriptionItem(
    val medicationId: String,
    val tradeName: String,
    val genericName: String,
    val dosage: String, // ex: "1 comprimido"
    val frequency: String, // ex: "8/8 horas"
    val duration: String, // ex: "3 dias"
    val instructions: String, // ex: "Tomar após a refeição com leite ou água"
    val quantity: Int, // ex: 1 caixa
    val unitPriceKz: Double
) {
    val subtotalKz: Double
        get() = quantity * unitPriceKz

    val subtotalFormattedKz: String
        get() = "${Medication.formatKz(subtotalKz)} Kz"
}

data class Prescription(
    val code: String, // ex: "REC-2026-084"
    val patientId: String,
    val patientName: String,
    val patientBi: String,
    val coverageType: String,
    val doctorName: String,
    val diagnosis: String,
    val clinicalNotes: String = "",
    val items: List<PrescriptionItem>,
    val status: PrescriptionStatus = PrescriptionStatus.PENDING_DISPENSE,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalKz: Double
        get() = items.sumOf { it.subtotalKz }

    val totalFormattedKz: String
        get() = "${Medication.formatKz(totalKz)} Kz"
}
