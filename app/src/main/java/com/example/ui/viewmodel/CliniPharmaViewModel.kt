package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CliniPharmaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CliniPharmaViewModel(
    private val repository: CliniPharmaRepository = CliniPharmaRepository()
) : ViewModel() {

    val patients = repository.patients
    val medications = repository.medications
    val prescriptions = repository.prescriptions
    val sales = repository.sales
    val pendingCashierOrders = repository.pendingCashierOrders
    val isLanActive = repository.isLanActive
    val lastSyncTimestamp = repository.lastSyncTimestamp

    // Papel selecionado no topo
    private val _currentRole = MutableStateFlow(AppRole.RECEPTION)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Controle de Modais e Diálogos
    private val _showNewPatientDialog = MutableStateFlow(false)
    val showNewPatientDialog: StateFlow<Boolean> = _showNewPatientDialog.asStateFlow()

    private val _patientForTriage = MutableStateFlow<Patient?>(null)
    val patientForTriage: StateFlow<Patient?> = _patientForTriage.asStateFlow()

    private val _patientForDoctor = MutableStateFlow<Patient?>(null)
    val patientForDoctor: StateFlow<Patient?> = _patientForDoctor.asStateFlow()

    private val _orderForThermalReceipt = MutableStateFlow<SaleOrder?>(null)
    val orderForThermalReceipt: StateFlow<SaleOrder?> = _orderForThermalReceipt.asStateFlow()

    private val _medicationForRestock = MutableStateFlow<Medication?>(null)
    val medicationForRestock: StateFlow<Medication?> = _medicationForRestock.asStateFlow()

    private val _showShiftSummaryDialog = MutableStateFlow(false)
    val showShiftSummaryDialog: StateFlow<Boolean> = _showShiftSummaryDialog.asStateFlow()

    // Toast / Feedback em Banner / Snackbar
    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    // Sincronização LAN
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun switchRole(role: AppRole) {
        _currentRole.value = role
        // Se mudou para médico e já há paciente selecionado, mantém; caso contrário, limpa
        showFeedback("Módulo alterado para ${role.title}")
    }

    fun openNewPatientDialog() {
        _showNewPatientDialog.value = true
    }

    fun closeNewPatientDialog() {
        _showNewPatientDialog.value = false
    }

    fun openTriageFor(patient: Patient) {
        _patientForTriage.value = patient
    }

    fun closeTriageDialog() {
        _patientForTriage.value = null
    }

    fun selectPatientForDoctor(patient: Patient) {
        _patientForDoctor.value = patient
        repository.updatePatientStatus(patient.id, PatientStatus.IN_CONSULTATION)
    }

    fun clearPatientForDoctor() {
        _patientForDoctor.value = null
    }

    fun openThermalReceipt(order: SaleOrder) {
        _orderForThermalReceipt.value = order
    }

    fun closeThermalReceipt() {
        _orderForThermalReceipt.value = null
    }

    fun openRestockDialog(medication: Medication) {
        _medicationForRestock.value = medication
    }

    fun closeRestockDialog() {
        _medicationForRestock.value = null
    }

    fun openShiftSummary() {
        _showShiftSummaryDialog.value = true
    }

    fun closeShiftSummary() {
        _showShiftSummaryDialog.value = false
    }

    fun showFeedback(msg: String) {
        _userFeedback.value = msg
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }

    // Ações de Negócio
    fun registerNewPatient(
        name: String,
        biNumber: String,
        phone: String,
        age: Int,
        gender: String,
        coverageType: String,
        chiefComplaint: String
    ) {
        val patient = repository.addPatient(
            name = name,
            biNumber = biNumber,
            phone = phone,
            age = age,
            gender = gender,
            coverageType = coverageType,
            chiefComplaint = chiefComplaint
        )
        closeNewPatientDialog()
        showFeedback("Paciente ${patient.name} cadastrado com sucesso!")
    }

    fun submitTriage(
        patientId: String,
        systolic: Int,
        diastolic: Int,
        temp: Double,
        weight: Double,
        height: Double,
        glucose: Int?,
        painScale: Int,
        notes: String
    ) {
        repository.saveTriage(
            patientId = patientId,
            systolicBP = systolic,
            diastolicBP = diastolic,
            temperatureCelsius = temp,
            weightKg = weight,
            heightCm = height,
            bloodGlucoseMgDl = glucose,
            painScale = painScale,
            nurseNotes = notes
        )
        closeTriageDialog()
        showFeedback("Triagem salva! Paciente encaminhado para o Consultório.")
    }

    fun finalizeDoctorConsultation(
        patientId: String,
        diagnosis: String,
        notes: String,
        prescribedItems: List<PrescriptionItem>
    ) {
        val prescription = repository.completeConsultation(
            patientId = patientId,
            diagnosis = diagnosis,
            doctorNotes = notes,
            prescribedItems = prescribedItems
        )
        clearPatientForDoctor()
        if (prescription != null) {
            showFeedback("Prescrição ${prescription.code} enviada para a Farmácia com sucesso!")
        } else {
            showFeedback("Consulta finalizada com sucesso!")
        }
    }

    fun dispensePrescription(code: String) {
        val result = repository.dispensePrescription(code)
        result.onSuccess { order ->
            showFeedback("Receita $code dispensada! Pedido ${order.invoiceNumber} enviado ao Caixa.")
        }.onFailure { err ->
            showFeedback("Erro ao dispensar: ${err.message}")
        }
    }

    fun submitDirectSale(
        customerName: String,
        customerBi: String,
        coverageType: String,
        items: List<CartItem>
    ) {
        val result = repository.submitDirectSaleToCashier(
            customerName = customerName,
            customerBi = customerBi,
            coverageType = coverageType,
            cartItems = items
        )
        result.onSuccess { order ->
            showFeedback("Venda enviada ao Caixa! Factura Provisória: ${order.invoiceNumber}")
            // Direciona opcionalmente para o Caixa para faturar
            _currentRole.value = AppRole.CASHIER
        }.onFailure { err ->
            showFeedback("Erro na venda: ${err.message}")
        }
    }

    fun restock(medicationId: String, qty: Int) {
        repository.restockMedication(medicationId, qty)
        closeRestockDialog()
        showFeedback("Estoque atualizado com sucesso!")
    }

    fun processPayment(
        orderId: String,
        method: PaymentMethod,
        amountPaid: Double,
        referenceOrPhone: String
    ) {
        val result = repository.finalizePayment(orderId, method, amountPaid, referenceOrPhone)
        result.onSuccess { finalizedOrder ->
            showFeedback("Pagamento concluído! Factura ${finalizedOrder.invoiceNumber} emitida.")
            openThermalReceipt(finalizedOrder)
        }.onFailure { err ->
            showFeedback("Erro no pagamento: ${err.message}")
        }
    }

    fun getShiftSummary(): CashShiftSummary {
        return repository.getShiftSummary()
    }

    fun syncLan() {
        viewModelScope.launch {
            _isSyncing.value = true
            kotlinx.coroutines.delay(1200) // Simulação realista de sincronismo com servidor local hospitalar
            repository.triggerLanSync()
            _isSyncing.value = false
            showFeedback("Sincronização concluída com a Rede Local da Clínica!")
        }
    }
}
