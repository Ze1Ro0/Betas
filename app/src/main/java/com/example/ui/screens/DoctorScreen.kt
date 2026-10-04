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
import com.example.data.model.*
import com.example.ui.components.BPStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorScreen(
    patients: List<Patient>,
    medications: List<Medication>,
    selectedPatient: Patient?,
    onSelectPatient: (Patient) -> Unit,
    onBackToQueue: () -> Unit,
    onFinalizeConsultation: (
        patientId: String,
        diagnosis: String,
        notes: String,
        prescribedItems: List<PrescriptionItem>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedPatient == null) {
        // Exibe fila de espera do médico
        DoctorQueueView(
            patients = patients.filter {
                it.status == PatientStatus.WAITING_CONSULTATION || it.status == PatientStatus.IN_CONSULTATION
            },
            onSelectPatient = onSelectPatient,
            modifier = modifier
        )
    } else {
        // Exibe PEP (Prontuário Eletrónico e Construtor de Prescrição)
        DoctorConsultationView(
            patient = selectedPatient,
            medications = medications,
            onBackToQueue = onBackToQueue,
            onFinalize = { diagnosis, notes, items ->
                onFinalizeConsultation(selectedPatient.id, diagnosis, notes, items)
            },
            modifier = modifier
        )
    }
}

@Composable
fun DoctorQueueView(
    patients: List<Patient>,
    onSelectPatient: (Patient) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Consultório Médico - Dra. Luísa Kiala",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Pacientes triados aguardando consulta médica e prescrição",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (patients.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Fila de Consulta Vazia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Não há pacientes aguardando atendimento médico no momento.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(patients, key = { it.id }) { patient ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPatient(patient) }
                        .testTag("doctor_queue_item_${patient.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = patient.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "BI: ${patient.biNumber} • ${patient.age} anos • ${patient.coverageType}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { onSelectPatient(patient) },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Atender PEP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (patient.chiefComplaint.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Queixa: ${patient.chiefComplaint}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Resumo dos Sinais Vitais da Triagem
                        if (patient.triage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "PA: ${patient.triage.bloodPressureFormatted}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Temp: ${patient.triage.temperatureCelsius}°C",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (patient.triage.isFever) Color(0xFFDC2626) else Color.Unspecified
                                    )
                                    Text(
                                        text = "Glic: ${patient.triage.bloodGlucoseMgDl ?: "-"} mg/dL",
                                        fontSize = 11.sp
                                    )
                                }

                                BPStatusBadge(status = patient.triage.bpStatus)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorConsultationView(
    patient: Patient,
    medications: List<Medication>,
    onBackToQueue: () -> Unit,
    onFinalize: (diagnosis: String, notes: String, items: List<PrescriptionItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    var anamneseNotes by remember { mutableStateOf(patient.chiefComplaint) }
    var diagnosisText by remember { mutableStateOf("Malária Não Complicada (P. falciparum)") }
    var doctorNotes by remember { mutableStateOf("") }

    // Construtor de Prescrição
    val prescribedItems = remember { mutableStateListOf<PrescriptionItem>() }

    // Seletor de Medicamentos do Estoque
    var medSearchQuery by remember { mutableStateOf("") }
    var selectedMedication by remember { mutableStateOf<Medication?>(null) }
    var dosage by remember { mutableStateOf("1 comprimido") }
    var frequency by remember { mutableStateOf("De 12 em 12 horas") }
    var duration by remember { mutableStateOf("3 dias") }
    var instructions by remember { mutableStateOf("Tomar com água após refeição") }
    var quantityStr by remember { mutableStateOf("1") }

    val commonDiagnoses = listOf(
        "Malária Não Complicada (P. falciparum)",
        "Hipertensão Arterial Sistémica",
        "Infeção Respiratória Aguda (IRA)",
        "Gastroenterite Aguda",
        "Febre Tifóide",
        "Diabetes Mellitus Tipo 2"
    )

    val filteredMeds = if (medSearchQuery.isBlank()) {
        emptyList()
    } else {
        medications.filter {
            it.tradeName.contains(medSearchQuery, ignoreCase = true) ||
                    it.genericName.contains(medSearchQuery, ignoreCase = true) ||
                    it.category.contains(medSearchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Barra Superior do Paciente Ativo
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = patient.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "(${patient.age} anos)",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = "BI: ${patient.biNumber} • Cobertura: ${patient.coverageType} • Tel: ${patient.phone}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    OutlinedButton(
                        onClick = onBackToQueue,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fila", fontSize = 11.sp)
                    }
                }
            }
        }

        // Seção 1: Sinais Vitais da Triagem
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Sinais Vitais da Triagem de Enfermagem",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (patient.triage != null) {
                        val t = patient.triage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VitalCard("Tensão (PA)", t.bloodPressureFormatted, t.bpStatus.label, t.bpStatus.isSevere, Modifier.weight(1f))
                            VitalCard("Temperatura", "${t.temperatureCelsius}°C", if (t.isFever) "Febre" else "Normal", t.isFever, Modifier.weight(1f))
                            VitalCard("Glicemia", "${t.bloodGlucoseMgDl ?: "-"} mg/dL", "Glicemia", false, Modifier.weight(1f))
                            VitalCard("IMC / Peso", "${t.bmiFormatted} (${t.weightKg}kg)", "Índice", false, Modifier.weight(1f))
                        }

                        if (t.nurseNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Notas do Enfermeiro: ${t.nurseNotes}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(text = "Paciente não possui triagem registrada.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Seção 2: Diagnóstico e Anamnese
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "2. Anamnese & Diagnóstico Clínico",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Chips de Diagnósticos Frequentes em Angola
                    Text(text = "Diagnósticos Frequentes (Clique para aplicar):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        commonDiagnoses.chunked(2).forEach { rowList ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rowList.forEach { diag ->
                                    val isSelected = diagnosisText == diag
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { diagnosisText = diag },
                                        label = { Text(diag, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = diagnosisText,
                        onValueChange = { diagnosisText = it },
                        label = { Text("Diagnóstico Final / Hipótese Diagnóstica *") },
                        modifier = Modifier.fillMaxWidth().testTag("doctor_diagnosis_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = doctorNotes,
                        onValueChange = { doctorNotes = it },
                        label = { Text("Recomendações e Conduta Médica") },
                        placeholder = { Text("Ex: Repouso, hidratação abundante, retorno se persistir febre...") },
                        modifier = Modifier.fillMaxWidth().height(70.dp),
                        maxLines = 3
                    )
                }
            }
        }

        // Seção 3: Construtor de Prescrição Digital Integrada com a Farmácia
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Prescrição Digital Integrada à Farmácia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Text(
                            text = "${prescribedItems.size} item(ns)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Busca de Medicamentos
                    OutlinedTextField(
                        value = medSearchQuery,
                        onValueChange = { medSearchQuery = it },
                        label = { Text("Buscar Medicamento na Farmácia Interna") },
                        placeholder = { Text("Ex: Coartem, Paracetamol, Amoxicilina...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (medSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { medSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("doctor_med_search_input"),
                        singleLine = true
                    )

                    // Resultados da Busca de Medicamentos com Indicador de Estoque em Tempo Real
                    if (filteredMeds.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                filteredMeds.take(4).forEach { med ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedMedication = med
                                                medSearchQuery = med.tradeName
                                            }
                                            .padding(vertical = 6.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = med.tradeName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(text = "${med.genericName} • ${med.priceFormattedKz}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        // Badge de Estoque em Tempo Real
                                        val stockText = if (med.isOutOfStock) "Esgotado" else "Estoque: ${med.stockQuantity} un"
                                        val stockColor = if (med.isOutOfStock) Color(0xFFDC2626) else Color(0xFF059669)
                                        val stockBg = if (med.isOutOfStock) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(stockBg)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = stockText,
                                                color = stockColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Seletor de Posologia
                    if (selectedMedication != null) {
                        val med = selectedMedication!!
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Definir Posologia para: ${med.tradeName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = dosage,
                                        onValueChange = { dosage = it },
                                        label = { Text("Dosagem", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = frequency,
                                        onValueChange = { frequency = it },
                                        label = { Text("Frequência", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = duration,
                                        onValueChange = { duration = it },
                                        label = { Text("Duração", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = quantityStr,
                                        onValueChange = { if (it.all { c -> c.isDigit() }) quantityStr = it },
                                        label = { Text("Qtd (Cx)", fontSize = 10.sp) },
                                        modifier = Modifier.weight(0.6f),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = instructions,
                                    onValueChange = { instructions = it },
                                    label = { Text("Instruções de Tomada", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        val qty = quantityStr.toIntOrNull() ?: 1
                                        prescribedItems.add(
                                            PrescriptionItem(
                                                medicationId = med.id,
                                                tradeName = med.tradeName,
                                                genericName = med.genericName,
                                                dosage = dosage,
                                                frequency = frequency,
                                                duration = duration,
                                                instructions = instructions,
                                                quantity = qty,
                                                unitPriceKz = med.unitPriceKz
                                            )
                                        )
                                        // Reset
                                        selectedMedication = null
                                        medSearchQuery = ""
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("add_prescription_item_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Adicionar à Receita", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Lista de Itens Já Adicionados à Receita
                    if (prescribedItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Medicamentos Prescritos:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))

                        prescribedItems.forEachIndexed { index, item ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "${index + 1}. ${item.tradeName} (${item.quantity} cx)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = "${item.dosage} • ${item.frequency} • ${item.duration}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "Obs: ${item.instructions}", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }

                                    IconButton(
                                        onClick = { prescribedItems.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Seção 4: Ação de Finalização
        item {
            Button(
                onClick = {
                    onFinalize(diagnosisText, doctorNotes, prescribedItems.toList())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("finalize_consultation_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (prescribedItems.isNotEmpty()) "Finalizar e Enviar para a Farmácia" else "Finalizar Consulta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun VitalCard(
    title: String,
    value: String,
    statusText: String,
    isAlert: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isAlert) Color(0xFFFEE2E2) else Color(0xFFF1F5F9)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isAlert) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = statusText,
                fontSize = 8.5.sp,
                color = if (isAlert) Color(0xFFDC2626) else Color(0xFF64748B)
            )
        }
    }
}
