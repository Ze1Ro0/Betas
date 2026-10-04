package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SaleOrder
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ThermalReceiptDialog(
    order: SaleOrder,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Date(order.timestamp))

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
                    .padding(16.dp)
            ) {
                // Header com fechar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Talão Fiscal de Impressão (80mm)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Área que simula a fita de papel térmico
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFCFBF9)) // Tom de papel térmico
                        .border(1.dp, Color(0xFFE2E0D8), RoundedCornerShape(8.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Cabeçalho da Empresa
                        Text(
                            text = "CLÍNICA & FARMÁCIA INTEGRADA\nDE LUANDA, LDA.",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "NIF: 5412093842 • Luanda, Angola\nRua Direita da Samba, Bairro Azul\nTel: +244 222 345 678 / 923 112 233",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF475569)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "FACTURA-RECIBO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "${order.invoiceNumber} (Original)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(4.dp))

                        // Dados da Operação
                        ThermalTextRow("Data/Hora:", formattedDate)
                        ThermalTextRow("Operador:", order.cashierName)
                        ThermalTextRow("Cliente:", order.patientName)
                        ThermalTextRow("NIF/BI:", order.patientBi)
                        if (order.coverageType != "Particular") {
                            ThermalTextRow("Convénio:", order.coverageType)
                        }
                        if (!order.prescriptionCode.isNullOrBlank()) {
                            ThermalTextRow("Receita Méd:", order.prescriptionCode)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(4.dp))

                        // Cabeçalho dos Itens
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "ITEM / LOTE", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(text = "QTD x PREÇO", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(text = "TOTAL (Kz)", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))

                        // Linhas de Itens
                        order.items.forEach { item ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(
                                    text = item.tradeName,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "  Lot:${item.batchNumber}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "${item.quantity} x ${com.example.data.model.Medication.formatKz(item.unitPriceKz)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = item.subtotalFormattedKz,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(4.dp))

                        // Totais Fiscais
                        ThermalTextRow("Total Ilíquido:", order.subtotalFormattedKz)
                        ThermalTextRow("IVA (Isento Art. 12 CIVA):", "0,00 Kz")

                        if (order.coverageType != "Particular") {
                            ThermalTextRow("Comparticipação Seguradora:", "${com.example.data.model.Medication.formatKz(order.copayInsuranceKz)} Kz")
                            ThermalTextRow("A Pagar pelo Utente:", "${com.example.data.model.Medication.formatKz(order.copayPatientKz)} Kz", isBold = true)
                        } else {
                            ThermalTextRow("TOTAL A PAGAR:", order.totalFormattedKz, isBold = true, fontSize = 12)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        ThermalTextRow("Forma Pagamento:", order.paymentMethod.displayName)
                        if (order.referenceOrPhone.isNotBlank()) {
                            ThermalTextRow("Ref / Terminal:", order.referenceOrPhone)
                        }
                        ThermalTextRow("Valor Entregue:", "${com.example.data.model.Medication.formatKz(order.amountPaidKz)} Kz")
                        ThermalTextRow("Troco:", order.changeFormattedKz)

                        Spacer(modifier = Modifier.height(8.dp))
                        DashedDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        // Selo Fiscal e Hash AGT
                        Text(
                            text = order.agtCertification,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Obrigado pela sua visita!\nAs melhoras e rápida recuperação.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Botões de Ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Factura ${order.invoiceNumber} enviada para partilha!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Partilhar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Imprimindo talão na impressora térmica 80mm...", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .testTag("print_thermal_receipt_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Imprimir Talão", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ThermalTextRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    fontSize: Int = 10
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF1E293B)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun DashedDivider() {
    Text(
        text = "------------------------------------------",
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        color = Color(0xFF94A3B8),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
