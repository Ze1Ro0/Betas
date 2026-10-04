package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Thermostat
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.BPStatus
import com.example.data.model.Patient
import java.util.Locale

@Composable
fun TriageDialog(
    patient: Patient,
    onDismiss: () -> Unit,
    onSaveTriage: (
        systolic: Int,
        diastolic: Int,
        temp: Double,
        weight: Double,
        height: Double,
        glucose: Int?,
        painScale: Int,
        notes: String
    ) -> Unit
) {
    var systolicStr by remember { mutableStateOf("120") }
    var diastolicStr by remember { mutableStateOf("80") }
    var tempStr by remember { mutableStateOf("37.2") }
    var weightStr by remember { mutableStateOf("65.0") }
    var heightStr by remember { mutableStateOf("168.0") }
    var glucoseStr by remember { mutableStateOf("95") }
    var painScale by remember { mutableStateOf(2f) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cálculos dinâmicos em tempo real
    val systolic = systolicStr.toIntOrNull() ?: 120
    val diastolic = diastolicStr.toIntOrNull() ?: 80
    val temp = tempStr.toDoubleOrNull() ?: 37.0
    val weight = weightStr.toDoubleOrNull() ?: 0.0
    val height = heightStr.toDoubleOrNull() ?: 0.0

    val bpStatus = when {
        systolic >= 160 || diastolic >= 100 -> BPStatus.STAGE_2_HYPERTENSION
        systolic >= 140 || diastolic >= 90 -> BPStatus.STAGE_1_HYPERTENSION
        systolic >= 130 || diastolic >= 85 -> BPStatus.PRE_HYPERTENSION
        systolic < 90 || diastolic < 60 -> BPStatus.HYPOTENSION
        else -> BPStatus.NORMAL
    }

    val bmi = if (height > 0) weight / ((height / 100.0) * (height / 100.0)) else 0.0
    val isFever = temp >= 37.8

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Triagem de Enfermagem",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${patient.name} (${patient.age} anos • ${patient.biNumber})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Resumo Alerta Dinâmico
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Avaliação Instantânea:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BPStatusBadge(status = bpStatus)
                                if (isFever) {
                                    Text(
                                        text = "FEBRE DETETADA",
                                        color = Color(0xFFB91C1C),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        if (bmi > 0) {
                            Text(
                                text = "IMC: ${String.format(Locale.US, "%.1f", bmi)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Tensão Arterial (PA)
                Text(
                    text = "Tensão Arterial (PA em mmHg) *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = systolicStr,
                        onValueChange = { systolicStr = it },
                        label = { Text("Sistólica") },
                        placeholder = { Text("120") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("triage_systolic_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = diastolicStr,
                        onValueChange = { diastolicStr = it },
                        label = { Text("Diastólica") },
                        placeholder = { Text("80") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("triage_diastolic_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Temperatura e Glicemia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tempStr,
                        onValueChange = { tempStr = it },
                        label = { Text("Temperatura (°C) *") },
                        placeholder = { Text("36.5") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("triage_temp_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = glucoseStr,
                        onValueChange = { glucoseStr = it },
                        label = { Text("Glicemia (mg/dL)") },
                        placeholder = { Text("90") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("triage_glucose_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Peso e Altura
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("Peso (kg) *") },
                        placeholder = { Text("70") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("triage_weight_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = heightStr,
                        onValueChange = { heightStr = it },
                        label = { Text("Altura (cm)") },
                        placeholder = { Text("170") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("triage_height_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Escala de Dor (0 a 10)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Escala de Dor:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${painScale.toInt()} / 10",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = painScale,
                    onValueChange = { painScale = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Observações da Enfermagem
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas de Enfermagem") },
                    placeholder = { Text("Paciente relata calafrios, tosse seca, etc.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    maxLines = 3
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botão Salvar
                Button(
                    onClick = {
                        val s = systolicStr.toIntOrNull()
                        val d = diastolicStr.toIntOrNull()
                        val t = tempStr.toDoubleOrNull()
                        val w = weightStr.toDoubleOrNull()
                        val h = heightStr.toDoubleOrNull() ?: 170.0
                        val g = glucoseStr.toIntOrNull()

                        if (s == null || d == null || t == null || w == null) {
                            errorMessage = "Preencha PA, Temperatura e Peso corretamente."
                        } else {
                            errorMessage = null
                            onSaveTriage(s, d, t, w, h, g, painScale.toInt(), notes)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_triage_button")
                ) {
                    Text("Salvar Triagem e Encaminhar ao Médico", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
