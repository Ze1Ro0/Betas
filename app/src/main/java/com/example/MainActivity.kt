package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppRole
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CliniPharmaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CliniPharmaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CliniPharmaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CliniPharmaApp(viewModel: CliniPharmaViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val medications by viewModel.medications.collectAsStateWithLifecycle()
    val prescriptions by viewModel.prescriptions.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val pendingCashierOrders by viewModel.pendingCashierOrders.collectAsStateWithLifecycle()
    val isLanActive by viewModel.isLanActive.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    // Modais
    val showNewPatientDialog by viewModel.showNewPatientDialog.collectAsStateWithLifecycle()
    val patientForTriage by viewModel.patientForTriage.collectAsStateWithLifecycle()
    val patientForDoctor by viewModel.patientForDoctor.collectAsStateWithLifecycle()
    val orderForThermalReceipt by viewModel.orderForThermalReceipt.collectAsStateWithLifecycle()
    val medicationForRestock by viewModel.medicationForRestock.collectAsStateWithLifecycle()
    val userFeedback by viewModel.userFeedback.collectAsStateWithLifecycle()

    // Back handling suave: se estiver numa sub-tela ou papel secundário, volta para Recepção
    BackHandler(enabled = patientForDoctor != null || currentRole != AppRole.RECEPTION) {
        if (patientForDoctor != null) {
            viewModel.clearPatientForDoctor()
        } else if (currentRole != AppRole.RECEPTION) {
            viewModel.switchRole(AppRole.RECEPTION)
        }
    }

    // Auto-dismiss do feedback toast após 3.5 segundos
    LaunchedEffect(userFeedback) {
        if (userFeedback != null) {
            kotlinx.coroutines.delay(3500)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopRoleBar(
                currentRole = currentRole,
                onRoleSelected = { viewModel.switchRole(it) },
                isLanActive = isLanActive,
                isSyncing = isSyncing,
                onSyncClick = { viewModel.syncLan() },
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Transição entre Módulos
            AnimatedContent(
                targetState = currentRole,
                label = "role_screen_transition"
            ) { role ->
                when (role) {
                    AppRole.RECEPTION -> {
                        ReceptionScreen(
                            patients = patients,
                            onNewPatientClick = { viewModel.openNewPatientDialog() },
                            onStartTriage = { viewModel.openTriageFor(it) },
                            onSendToDoctor = { patient ->
                                viewModel.selectPatientForDoctor(patient)
                                viewModel.switchRole(AppRole.DOCTOR)
                            }
                        )
                    }

                    AppRole.DOCTOR -> {
                        DoctorScreen(
                            patients = patients,
                            medications = medications,
                            selectedPatient = patientForDoctor,
                            onSelectPatient = { viewModel.selectPatientForDoctor(it) },
                            onBackToQueue = { viewModel.clearPatientForDoctor() },
                            onFinalizeConsultation = { patientId, diagnosis, notes, items ->
                                viewModel.finalizeDoctorConsultation(patientId, diagnosis, notes, items)
                            }
                        )
                    }

                    AppRole.PHARMACY -> {
                        PharmacyScreen(
                            medications = medications,
                            prescriptions = prescriptions,
                            onDispensePrescription = { code ->
                                viewModel.dispensePrescription(code)
                            },
                            onSubmitDirectSale = { customerName, customerBi, coverage, items ->
                                viewModel.submitDirectSale(customerName, customerBi, coverage, items)
                            },
                            onOpenRestock = { med ->
                                viewModel.openRestockDialog(med)
                            }
                        )
                    }

                    AppRole.CASHIER -> {
                        CashierScreen(
                            pendingOrders = pendingCashierOrders,
                            completedSales = sales,
                            onProcessPayment = { orderId, method, amountPaid, ref ->
                                viewModel.processPayment(orderId, method, amountPaid, ref)
                            },
                            onViewReceipt = { order ->
                                viewModel.openThermalReceipt(order)
                            },
                            getShiftSummary = {
                                viewModel.getShiftSummary()
                            }
                        )
                    }
                }
            }

            // Toast / Banner de Feedback no Topo
            AnimatedVisibility(
                visible = userFeedback != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F172A),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF10B981))
                        )
                        Text(
                            text = userFeedback ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Modais e Diálogos
            if (showNewPatientDialog) {
                NewPatientDialog(
                    onDismiss = { viewModel.closeNewPatientDialog() },
                    onSave = { name, bi, phone, age, gender, coverage, complaint ->
                        viewModel.registerNewPatient(name, bi, phone, age, gender, coverage, complaint)
                    }
                )
            }

            if (patientForTriage != null) {
                TriageDialog(
                    patient = patientForTriage!!,
                    onDismiss = { viewModel.closeTriageDialog() },
                    onSaveTriage = { s, d, t, w, h, g, pain, notes ->
                        viewModel.submitTriage(patientForTriage!!.id, s, d, t, w, h, g, pain, notes)
                    }
                )
            }

            if (medicationForRestock != null) {
                RestockDialog(
                    medication = medicationForRestock!!,
                    onDismiss = { viewModel.closeRestockDialog() },
                    onRestock = { qty ->
                        viewModel.restock(medicationForRestock!!.id, qty)
                    }
                )
            }

            if (orderForThermalReceipt != null) {
                ThermalReceiptDialog(
                    order = orderForThermalReceipt!!,
                    onDismiss = { viewModel.closeThermalReceipt() }
                )
            }
        }
    }
}
