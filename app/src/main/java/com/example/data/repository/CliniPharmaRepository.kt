package com.example.data.repository

import com.example.data.local.InitialData
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.*

class CliniPharmaRepository {

    private val _patients = MutableStateFlow<List<Patient>>(InitialData.getInitialPatients())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _medications = MutableStateFlow<List<Medication>>(InitialData.getInitialMedications())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val _prescriptions = MutableStateFlow<List<Prescription>>(InitialData.getInitialPrescriptions())
    val prescriptions: StateFlow<List<Prescription>> = _prescriptions.asStateFlow()

    private val _sales = MutableStateFlow<List<SaleOrder>>(InitialData.getInitialSales())
    val sales: StateFlow<List<SaleOrder>> = _sales.asStateFlow()

    private val _pendingCashierOrders = MutableStateFlow<List<SaleOrder>>(emptyList())
    val pendingCashierOrders: StateFlow<List<SaleOrder>> = _pendingCashierOrders.asStateFlow()

    private val _isLanActive = MutableStateFlow(true)
    val isLanActive: StateFlow<Boolean> = _isLanActive.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private var nextPrescriptionNumber = 85
    private var nextInvoiceNumber = 419

    // --- Pacientes & Triagem ---
    fun addPatient(
        name: String,
        biNumber: String,
        phone: String,
        age: Int,
        gender: String,
        coverageType: String,
        chiefComplaint: String
    ): Patient {
        val newPatient = Patient(
            id = "pat_${System.currentTimeMillis() % 10000}",
            name = name.trim(),
            biNumber = biNumber.trim().uppercase(),
            phone = phone.trim(),
            age = age,
            gender = gender,
            coverageType = coverageType,
            status = PatientStatus.WAITING_TRIAGE,
            chiefComplaint = chiefComplaint.trim(),
            createdAt = System.currentTimeMillis()
        )
        _patients.update { listOf(newPatient) + it }
        return newPatient
    }

    fun updatePatientStatus(patientId: String, newStatus: PatientStatus) {
        _patients.update { list ->
            list.map {
                if (it.id == patientId) it.copy(status = newStatus) else it
            }
        }
    }

    fun saveTriage(
        patientId: String,
        systolicBP: Int,
        diastolicBP: Int,
        temperatureCelsius: Double,
        weightKg: Double,
        heightCm: Double,
        bloodGlucoseMgDl: Int?,
        painScale: Int,
        nurseNotes: String
    ) {
        val triage = Triage(
            patientId = patientId,
            systolicBP = systolicBP,
            diastolicBP = diastolicBP,
            temperatureCelsius = temperatureCelsius,
            weightKg = weightKg,
            heightCm = heightCm,
            bloodGlucoseMgDl = bloodGlucoseMgDl,
            painScale = painScale,
            nurseNotes = nurseNotes,
            recordedBy = "Enf. Carlos Benguela",
            timestamp = System.currentTimeMillis()
        )

        _patients.update { list ->
            list.map { patient ->
                if (patient.id == patientId) {
                    patient.copy(
                        triage = triage,
                        status = PatientStatus.WAITING_CONSULTATION // Avança automaticamente na fila
                    )
                } else patient
            }
        }
    }

    // --- Consultório & Prescrição ---
    fun completeConsultation(
        patientId: String,
        diagnosis: String,
        doctorNotes: String,
        prescribedItems: List<PrescriptionItem>
    ): Prescription? {
        val patient = _patients.value.find { it.id == patientId } ?: return null

        var prescription: Prescription? = null
        if (prescribedItems.isNotEmpty()) {
            val code = "REC-2026-%03d".format(Locale.US, nextPrescriptionNumber++)
            prescription = Prescription(
                code = code,
                patientId = patient.id,
                patientName = patient.name,
                patientBi = patient.biNumber,
                coverageType = patient.coverageType,
                doctorName = "Dra. Luísa Kiala",
                diagnosis = diagnosis,
                clinicalNotes = doctorNotes,
                items = prescribedItems,
                status = PrescriptionStatus.PENDING_DISPENSE,
                createdAt = System.currentTimeMillis()
            )
            _prescriptions.update { listOf(prescription) + it }
        }

        _patients.update { list ->
            list.map { p ->
                if (p.id == patientId) {
                    p.copy(
                        status = PatientStatus.COMPLETED,
                        diagnosis = diagnosis,
                        doctorNotes = doctorNotes
                    )
                } else p
            }
        }

        return prescription
    }

    // --- Farmácia & FEFO ---
    fun dispensePrescription(prescriptionCode: String): Result<SaleOrder> {
        val prescription = _prescriptions.value.find { it.code == prescriptionCode }
            ?: return Result.failure(Exception("Receita $prescriptionCode não encontrada."))

        if (prescription.status == PrescriptionStatus.DISPENSED) {
            return Result.failure(Exception("Receita já foi dispensada anteriormente."))
        }

        // Validação de estoque e dedução FEFO
        var hasStockIssues = false
        val currentMeds = _medications.value.toMutableList()
        val cartItems = mutableListOf<CartItem>()

        for (item in prescription.items) {
            val medIndex = currentMeds.indexOfFirst { it.id == item.medicationId }
            if (medIndex != -1) {
                val med = currentMeds[medIndex]
                if (med.stockQuantity < item.quantity) {
                    hasStockIssues = true
                } else {
                    // Baixa de estoque FEFO (deduz do lote atual mais próximo do vencimento)
                    val updatedMed = med.copy(stockQuantity = med.stockQuantity - item.quantity)
                    currentMeds[medIndex] = updatedMed

                    cartItems.add(
                        CartItem(
                            medicationId = med.id,
                            tradeName = med.tradeName,
                            genericName = med.genericName,
                            batchNumber = med.batchNumber,
                            unitPriceKz = med.unitPriceKz,
                            quantity = item.quantity
                        )
                    )
                }
            } else {
                hasStockIssues = true
            }
        }

        if (hasStockIssues) {
            return Result.failure(Exception("Quantidade insuficiente de estoque para alguns itens da receita."))
        }

        // Atualiza estoque
        _medications.value = currentMeds

        // Marca receita como dispensada
        _prescriptions.update { list ->
            list.map {
                if (it.code == prescriptionCode) it.copy(status = PrescriptionStatus.DISPENSED) else it
            }
        }

        // Gera ordem de pagamento para o Caixa
        val subtotal = cartItems.sumOf { it.subtotalKz }
        val pendingOrder = SaleOrder(
            id = "ord_${System.currentTimeMillis() % 100000}",
            invoiceNumber = "FR 2026/%04d".format(Locale.US, nextInvoiceNumber++),
            prescriptionCode = prescription.code,
            patientName = prescription.patientName,
            patientBi = prescription.patientBi,
            coverageType = prescription.coverageType,
            items = cartItems,
            subtotalKz = subtotal,
            discountKz = 0.0,
            totalKz = subtotal,
            paymentMethod = PaymentMethod.CASH,
            amountPaidKz = subtotal,
            changeKz = 0.0,
            insuranceName = if (prescription.coverageType != "Particular") prescription.coverageType else "",
            copayInsuranceKz = if (prescription.coverageType != "Particular") subtotal * 0.8 else 0.0,
            copayPatientKz = if (prescription.coverageType != "Particular") subtotal * 0.2 else subtotal,
            cashierName = "Teresa Gomes",
            timestamp = System.currentTimeMillis()
        )

        _pendingCashierOrders.update { listOf(pendingOrder) + it }

        return Result.success(pendingOrder)
    }

    fun submitDirectSaleToCashier(
        customerName: String,
        customerBi: String,
        coverageType: String,
        cartItems: List<CartItem>
    ): Result<SaleOrder> {
        if (cartItems.isEmpty()) {
            return Result.failure(Exception("O carrinho de venda está vazio."))
        }

        // Dedução de estoque
        val currentMeds = _medications.value.toMutableList()
        for (item in cartItems) {
            val idx = currentMeds.indexOfFirst { it.id == item.medicationId }
            if (idx != -1) {
                val med = currentMeds[idx]
                if (med.stockQuantity < item.quantity) {
                    return Result.failure(Exception("Estoque insuficiente para ${med.tradeName}."))
                }
                currentMeds[idx] = med.copy(stockQuantity = med.stockQuantity - item.quantity)
            }
        }
        _medications.value = currentMeds

        val subtotal = cartItems.sumOf { it.subtotalKz }
        val pendingOrder = SaleOrder(
            id = "ord_${System.currentTimeMillis() % 100000}",
            invoiceNumber = "FR 2026/%04d".format(Locale.US, nextInvoiceNumber++),
            patientName = customerName.ifBlank { "Consumidor Final" },
            patientBi = customerBi.ifBlank { "999999999LA000" },
            coverageType = coverageType,
            items = cartItems,
            subtotalKz = subtotal,
            discountKz = 0.0,
            totalKz = subtotal,
            paymentMethod = PaymentMethod.CASH,
            amountPaidKz = subtotal,
            changeKz = 0.0,
            insuranceName = if (coverageType != "Particular") coverageType else "",
            copayInsuranceKz = if (coverageType != "Particular") subtotal * 0.8 else 0.0,
            copayPatientKz = if (coverageType != "Particular") subtotal * 0.2 else subtotal,
            cashierName = "Teresa Gomes",
            timestamp = System.currentTimeMillis()
        )

        _pendingCashierOrders.update { listOf(pendingOrder) + it }
        return Result.success(pendingOrder)
    }

    fun restockMedication(medicationId: String, addedQuantity: Int) {
        _medications.update { list ->
            list.map {
                if (it.id == medicationId) {
                    it.copy(stockQuantity = it.stockQuantity + addedQuantity)
                } else it
            }
        }
    }

    // --- Caixa & Facturação ---
    fun finalizePayment(
        orderId: String,
        paymentMethod: PaymentMethod,
        amountPaid: Double,
        referenceOrPhone: String
    ): Result<SaleOrder> {
        val pending = _pendingCashierOrders.value.find { it.id == orderId }
            ?: return Result.failure(Exception("Ordem de pagamento não encontrada."))

        val payableAmount = if (pending.coverageType != "Particular") pending.copayPatientKz else pending.totalKz
        val change = if (paymentMethod == PaymentMethod.CASH && amountPaid > payableAmount) {
            amountPaid - payableAmount
        } else 0.0

        val finalizedOrder = pending.copy(
            paymentMethod = paymentMethod,
            amountPaidKz = amountPaid,
            changeKz = change,
            referenceOrPhone = referenceOrPhone.trim(),
            timestamp = System.currentTimeMillis()
        )

        _pendingCashierOrders.update { it.filter { order -> order.id != orderId } }
        _sales.update { listOf(finalizedOrder) + it }

        return Result.success(finalizedOrder)
    }

    fun getShiftSummary(): CashShiftSummary {
        val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
        val completedSales = _sales.value

        var cashTotal = 0.0
        var tpaTotal = 0.0
        var mcxTotal = 0.0
        var insuranceTotal = 0.0

        for (sale in completedSales) {
            when (sale.paymentMethod) {
                PaymentMethod.CASH -> cashTotal += if (sale.coverageType != "Particular") sale.copayPatientKz else sale.totalKz
                PaymentMethod.TPA_MULTICAIXA -> tpaTotal += if (sale.coverageType != "Particular") sale.copayPatientKz else sale.totalKz
                PaymentMethod.MULTICAIXA_EXPRESS -> mcxTotal += if (sale.coverageType != "Particular") sale.copayPatientKz else sale.totalKz
                PaymentMethod.HEALTH_INSURANCE -> insuranceTotal += sale.copayInsuranceKz
            }
        }

        return CashShiftSummary(
            shiftDate = todayStr,
            cashierName = "Teresa Gomes",
            totalCashKz = cashTotal,
            totalTpaKz = tpaTotal,
            totalMcxKz = mcxTotal,
            totalInsuranceKz = insuranceTotal,
            totalTransactions = completedSales.size
        )
    }

    fun triggerLanSync() {
        _lastSyncTimestamp.value = System.currentTimeMillis()
    }
}
