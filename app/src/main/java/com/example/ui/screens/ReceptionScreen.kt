package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BPStatus
import com.example.data.model.Patient
import com.example.data.model.PatientStatus
import com.example.ui.components.BPStatusBadge
import com.example.ui.components.PatientStatusBadge

@Composable
fun ReceptionScreen(
    patients: List<Patient>,
    onNewPatientClick: () -> Unit,
    onStartTriage: (Patient) -> Unit,
    onSendToDoctor: (Patient) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<PatientStatus?>(null) }

    // Contadores rápidos por status
    val waitingTriageCount = patients.count { it.status == PatientStatus.WAITING_TRIAGE }
    val waitingConsultCount = patients.count { it.status == PatientStatus.WAITING_CONSULTATION }
    val inConsultCount = patients.count { it.status == PatientStatus.IN_CONSULTATION }
    val completedCount = patients.count { it.status == PatientStatus.COMPLETED }

    val filteredPatients = patients.filter { patient ->
        val matchesQuery = patient.name.contains(searchQuery, ignoreCase = true) ||
                patient.biNumber.contains(searchQuery, ignoreCase = true) ||
                patient.phone.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == null || patient.status == selectedFilter
        matchesQuery && matchesFilter
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabeçalho de Módulo e Boas-Vindas
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fila de Atendimento e Triagem",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Gestão de fluxo de pacientes na recepção da clínica",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onNewPatientClick,
                        modifier = Modifier.testTag("new_patient_header_button"),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Novo Paciente", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Cards de Métricas da Fila
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricChip(
                        title = "Triagem",
                        count = waitingTriageCount,
                        color = Color(0xFFD97706),
                        bgColor = Color(0xFFFEF3C7),
                        isSelected = selectedFilter == PatientStatus.WAITING_TRIAGE,
                        onClick = {
                            selectedFilter = if (selectedFilter == PatientStatus.WAITING_TRIAGE) null else PatientStatus.WAITING_TRIAGE
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        title = "Consultório",
                        count = waitingConsultCount,
                        color = Color(0xFF2563EB),
                        bgColor = Color(0xFFDBEAFE),
                        isSelected = selectedFilter == PatientStatus.WAITING_CONSULTATION,
                        onClick = {
                            selectedFilter = if (selectedFilter == PatientStatus.WAITING_CONSULTATION) null else PatientStatus.WAITING_CONSULTATION
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        title = "Em Consulta",
                        count = inConsultCount,
                        color = Color(0xFF7C3AED),
                        bgColor = Color(0xFFEDE9FE),
                        isSelected = selectedFilter == PatientStatus.IN_CONSULTATION,
                        onClick = {
                            selectedFilter = if (selectedFilter == PatientStatus.IN_CONSULTATION) null else PatientStatus.IN_CONSULTATION
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MetricChip(
                        title = "Concluídos",
                        count = completedCount,
                        color = Color(0xFF059669),
                        bgColor = Color(0xFFD1FAE5),
                        isSelected = selectedFilter == PatientStatus.COMPLETED,
                        onClick = {
                            selectedFilter = if (selectedFilter == PatientStatus.COMPLETED) null else PatientStatus.COMPLETED
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Barra de Busca
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar paciente por nome, BI ou telefone...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reception_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Lista de Pacientes
            if (filteredPatients.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhum paciente encontrado",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tente buscar por outro termo ou adicione um novo paciente.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredPatients, key = { it.id }) { patient ->
                    PatientQueueCard(
                        patient = patient,
                        onStartTriage = { onStartTriage(patient) },
                        onSendToDoctor = { onSendToDoctor(patient) }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNewPatientClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("new_patient_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Novo Paciente")
        }
    }
}

@Composable
private fun MetricChip(
    title: String,
    count: Int,
    color: Color,
    bgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .then(if (isSelected) Modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.2f)) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun PatientQueueCard(
    patient: Patient,
    onStartTriage: () -> Unit,
    onSendToDoctor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Linha Topo: Nome e Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = patient.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "BI: ${patient.biNumber} • ${patient.age} anos • ${patient.gender}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                PatientStatusBadge(status = patient.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Cobertura e Telefone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (patient.coverageType == "Particular") Color(0xFF64748B) else Color(0xFF0D9488)
                    )
                    Text(
                        text = patient.coverageType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (patient.coverageType == "Particular") Color(0xFF475569) else Color(0xFF0D9488)
                    )
                }

                Text(
                    text = patient.phone,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Queixa Principal
            if (patient.chiefComplaint.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Queixa: ${patient.chiefComplaint}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            // Dados da Triagem se já realizada
            if (patient.triage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // PA
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(text = "PA: ${patient.triage.bloodPressureFormatted}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Temp
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (patient.triage.isFever) Color(0xFFFEE2E2) else Color(0xFFF1F5F9))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${patient.triage.temperatureCelsius}°C",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (patient.triage.isFever) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Peso e IMC
                    Text(
                        text = "${patient.triage.weightKg} kg (IMC ${patient.triage.bmiFormatted})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    BPStatusBadge(status = patient.triage.bpStatus)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ações Conforme Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (patient.status) {
                    PatientStatus.WAITING_TRIAGE -> {
                        Button(
                            onClick = onStartTriage,
                            modifier = Modifier.testTag("start_triage_btn_${patient.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Thermostat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Realizar Triagem", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    PatientStatus.WAITING_CONSULTATION -> {
                        Button(
                            onClick = onSendToDoctor,
                            modifier = Modifier.testTag("send_doctor_btn_${patient.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chamar no Consultório", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    PatientStatus.IN_CONSULTATION -> {
                        OutlinedButton(
                            onClick = onSendToDoctor,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Retomar Consulta", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    PatientStatus.COMPLETED -> {
                        Text(
                            text = "Atendimento Concluído",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }
        }
    }
}
