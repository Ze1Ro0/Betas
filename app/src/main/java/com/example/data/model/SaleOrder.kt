package com.example.data.model

enum class PaymentMethod(val displayName: String, val code: String) {
    CASH("Numerário (Kz)", "NUM"),
    TPA_MULTICAIXA("TPA Multicaixa", "TPA"),
    MULTICAIXA_EXPRESS("Multicaixa Express", "MCX"),
    HEALTH_INSURANCE("Convénio / Seguro", "SEG")
}

data class CartItem(
    val medicationId: String,
    val tradeName: String,
    val genericName: String,
    val batchNumber: String,
    val unitPriceKz: Double,
    val quantity: Int
) {
    val subtotalKz: Double
        get() = quantity * unitPriceKz

    val subtotalFormattedKz: String
        get() = "${Medication.formatKz(subtotalKz)} Kz"
}

data class SaleOrder(
    val id: String,
    val invoiceNumber: String, // ex: "FR 2026/0419"
    val prescriptionCode: String? = null,
    val patientName: String,
    val patientBi: String,
    val coverageType: String,
    val items: List<CartItem>,
    val subtotalKz: Double,
    val discountKz: Double = 0.0,
    val ivaRate: Double = 0.0, // Medicamentos em Angola com isenção ou taxa reduzida
    val ivaAmountKz: Double = 0.0,
    val totalKz: Double,
    val paymentMethod: PaymentMethod,
    val amountPaidKz: Double,
    val changeKz: Double = 0.0,
    val referenceOrPhone: String = "", // Nº do Cartão/TPA ou Tel MCX Express
    val insuranceName: String = "",
    val copayInsuranceKz: Double = 0.0,
    val copayPatientKz: Double = 0.0,
    val cashierName: String = "Teresa Gomes",
    val timestamp: Long = System.currentTimeMillis(),
    val agtCertification: String = "4b8f-Processado por programa certificado n.º 281/AGT/2026"
) {
    val totalFormattedKz: String
        get() = "${Medication.formatKz(totalKz)} Kz"

    val subtotalFormattedKz: String
        get() = "${Medication.formatKz(subtotalKz)} Kz"

    val changeFormattedKz: String
        get() = "${Medication.formatKz(changeKz)} Kz"
}

data class CashShiftSummary(
    val shiftDate: String,
    val cashierName: String,
    val totalCashKz: Double,
    val totalTpaKz: Double,
    val totalMcxKz: Double,
    val totalInsuranceKz: Double,
    val totalTransactions: Int
) {
    val totalTurnoverKz: Double
        get() = totalCashKz + totalTpaKz + totalMcxKz + totalInsuranceKz

    val totalTurnoverFormattedKz: String
        get() = "${Medication.formatKz(totalTurnoverKz)} Kz"
}
