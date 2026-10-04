package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPatientDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        biNumber: String,
        phone: String,
        age: Int,
        gender: String,
        coverageType: String,
        chiefComplaint: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var biNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+244 ") }
    var ageStr by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Masculino") }
    var coverageType by remember { mutableStateOf("Particular") }
    var chiefComplaint by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coverageOptions = listOf(
        "Particular",
        "ENSA Seguros",
        "Victoria Seguros",
        "Nossa Seguros",
        "Fidelidade Angola"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabeçalho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Novo Paciente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar"
                        )
                    }
                }

                Text(
                    text = "Registo de admissão hospitalar e fila de triagem",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nome Completo
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Completo *") },
                    placeholder = { Text("Ex: Manuel Domingos Kiala") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bilhete de Identidade (BI)
                OutlinedTextField(
                    value = biNumber,
                    onValueChange = { biNumber = it.uppercase() },
                    label = { Text("Nº Bilhete de Identidade (BI) *") },
                    placeholder = { Text("Ex: 004829124LA042") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_bi_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Telefone & Idade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefone (+244) *") },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("patient_phone_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { if (it.all { char -> char.isDigit() }) ageStr = it },
                        label = { Text("Idade *") },
                        modifier = Modifier
                            .weight(0.7f)
                            .testTag("patient_age_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Género
                Text(
                    text = "Género",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Masculino", "Feminino").forEach { g ->
                        val isSelected = gender == g
                        FilterChip(
                            selected = isSelected,
                            onClick = { gender = g },
                            label = { Text(g) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Convénio / Seguro
                Text(
                    text = "Tipo de Cobertura / Seguro",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                var expandedInsurance by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedInsurance,
                    onExpandedChange = { expandedInsurance = !expandedInsurance }
                ) {
                    OutlinedTextField(
                        value = coverageType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInsurance) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedInsurance,
                        onDismissRequest = { expandedInsurance = false }
                    ) {
                        coverageOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    coverageType = opt
                                    expandedInsurance = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Queixa Principal
                OutlinedTextField(
                    value = chiefComplaint,
                    onValueChange = { chiefComplaint = it },
                    label = { Text("Queixa Principal / Motivo da Consulta") },
                    placeholder = { Text("Ex: Febre alta, calafrios e dores no corpo...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("patient_complaint_input"),
                    maxLines = 3
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botão Salvar
                Button(
                    onClick = {
                        val ageInt = ageStr.toIntOrNull()
                        if (name.isBlank() || biNumber.isBlank() || phone.isBlank() || ageInt == null) {
                            errorMessage = "Por favor, preencha todos os campos obrigatórios (*)."
                        } else {
                            errorMessage = null
                            onSave(
                                name,
                                biNumber,
                                phone,
                                ageInt,
                                gender,
                                coverageType,
                                chiefComplaint
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_patient_button")
                ) {
                    Text("Admitir Paciente na Fila", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
