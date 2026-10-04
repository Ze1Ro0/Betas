package com.example.data.model

enum class StockAlertLevel(val label: String) {
    EXPIRING_SOON("Vence em breve"),
    LOW_STOCK("Estoque Baixo (< 10)"),
    NORMAL("Regular"),
    OUT_OF_STOCK("Esgotado")
}

data class Medication(
    val id: String,
    val tradeName: String, // ex: Coartem 80/480mg
    val genericName: String, // ex: Arteméter + Lumefantrina
    val dosageForm: String, // Comprimidos, Suspensão Oral, Xarope, etc.
    val batchNumber: String, // Lote
    val expirationDate: String, // DD/MM/AAAA
    val daysUntilExpiry: Int, // Dias restantes
    val stockQuantity: Int, // Quantidade em armazém
    val minStockThreshold: Int = 10,
    val unitPriceKz: Double, // Valor em Kwanzas
    val category: String, // Antimaláricos, Antibióticos, etc.
    val barcode: String = ""
) {
    val isOutOfStock: Boolean
        get() = stockQuantity <= 0

    val isLowStock: Boolean
        get() = stockQuantity in 1 until minStockThreshold

    val isExpiringSoon: Boolean
        get() = daysUntilExpiry in 1..30

    val alertLevel: StockAlertLevel
        get() = when {
            stockQuantity <= 0 -> StockAlertLevel.OUT_OF_STOCK
            daysUntilExpiry in 1..30 -> StockAlertLevel.EXPIRING_SOON
            stockQuantity < minStockThreshold -> StockAlertLevel.LOW_STOCK
            else -> StockAlertLevel.NORMAL
        }

    val priceFormattedKz: String
        get() = "${formatKz(unitPriceKz)} Kz"

    companion object {
        fun formatKz(amount: Double): String {
            val longVal = amount.toLong()
            return String.format(java.util.Locale.GERMAN, "%,d", longVal)
        }
    }
}
