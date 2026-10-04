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
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(
    pendingOrders: List<SaleOrder>,
    completedSales: List<SaleOrder>,
    onProcessPayment: (orderId: String, method: PaymentMethod, amountPaid: Double, reference: String) -> Unit,
    onViewReceipt: (SaleOrder) -> Unit,
    getShiftSummary: () -> CashShiftSummary,
    modifier: Modifier = Modifier
) {
    var selectedOrder by remember { mutableStateOf<SaleOrder?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var amountPaidStr by remember { mutableStateOf("") }
    var referenceStr by remember { mutableStateOf("") }
    var showShiftReportModal by remember { mutableStateOf(false) }

    // Atualiza pedido selecionado caso a lista mude
    LaunchedEffect(pendingOrders) {
        if (selectedOrder != null && pendingOrders.none { it.id == selectedOrder!!.id }) {
            selectedOrder = pendingOrders.firstOrNull()
        } else if (selectedOrder == null && pendingOrders.isNotEmpty()) {
            selectedOrder = pendingOrders.first()
        }
    }

    // Atualiza valor padrão ao selecionar pedido
    LaunchedEffect(selectedOrder) {
        if (selectedOrder != null) {
            val payable = if (selectedOrder!!.coverageType != "Particular") {
                selectedOrder!!.copayPatientKz
            } else {
                selectedOrder!!.totalKz
            }
            amountPaidStr = payable.toLong().toString()
        }
    }

    val shiftSummary = remember(completedSales) { getShiftSummary() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabeçalho e Botão Fecho de Caixa
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Caixa & Facturação Rápida",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Operadora: Teresa Gomes • Terminal TPA #04",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showShiftReportModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("open_shift_summary_btn")
                ) {
                    Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Fecho de Turno", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Mini Card de Balanço do Turno
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Turno", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = shiftSummary.totalTurnoverFormattedKz, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(modifier = Modifier.height(24.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Numerário (Kz)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${Medication.formatKz(shiftSummary.totalCashKz)} Kz", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Divider(modifier = Modifier.height(24.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TPA Multicaixa", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${Medication.formatKz(shiftSummary.totalTpaKz)} Kz", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Seção: Ordens Pendentes de Pagamento
        item {
            Text(
                text = "Pedidos Aguardando Pagamento (${pendingOrders.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        if (pendingOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Nenhum pedido pendente", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Todas as receitas e vendas de balcão foram facturadas.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(pendingOrders, key = { it.id }) { order ->
                val isSelected = selectedOrder?.id == order.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedOrder = order }
                        .testTag("pending_order_${order.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = order.invoiceNumber, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                if (order.prescriptionCode != null) {
                                    Text(
                                        text = order.prescriptionCode,
                                        fontSize = 10.sp,
                                        color = Color(0xFF047857),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFD1FAE5)).padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(text = "${order.patientName} (${order.patientBi})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "${order.items.size} item(ns) • ${order.coverageType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = order.totalFormattedKz, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            if (order.coverageType != "Particular") {
                                Text(
                                    text = "Utente paga: ${Medication.formatKz(order.copayPatientKz)} Kz",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D9488)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Seção: Formulário de Pagamento da Ordem Selecionada
        if (selectedOrder != null) {
            val order = selectedOrder!!
            val payableAmount = if (order.coverageType != "Particular") order.copayPatientKz else order.totalKz
            val amountPaid = amountPaidStr.toDoubleOrNull() ?: payableAmount
            val change = if (selectedPaymentMethod == PaymentMethod.CASH && amountPaid > payableAmount) {
                amountPaid - payableAmount
            } else 0.0

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Checkout e Faturação: ${order.invoiceNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Cliente: ${order.patientName} • NIF: ${order.patientBi}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Resumo dos Itens
                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${item.quantity}x ${item.tradeName}", fontSize = 11.sp)
                                Text(text = item.subtotalFormattedKz, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Linha Total e Cobertura
                        if (order.coverageType != "Particular") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Total Ilíquido:", fontSize = 11.sp)
                                Text(text = order.totalFormattedKz, fontSize = 11.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Seguradora (${order.coverageType} 80%):", fontSize = 11.sp, color = Color(0xFF047857))
                                Text(text = "-${Medication.formatKz(order.copayInsuranceKz)} Kz", fontSize = 11.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "VALOR A COBRAR (Kz):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "${Medication.formatKz(payableAmount)} Kz",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Seletor de Formas de Pagamento
                        Text(text = "Forma de Pagamento em Angola *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            PaymentMethod.entries.forEach { method ->
                                val isSelected = selectedPaymentMethod == method
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                        .clickable {
                                            selectedPaymentMethod = method
                                            if (method != PaymentMethod.CASH) {
                                                amountPaidStr = payableAmount.toLong().toString()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("payment_method_${method.code.lowercase()}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedPaymentMethod = method }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = method.displayName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Campos Específicos por Meio de Pagamento
                        when (selectedPaymentMethod) {
                            PaymentMethod.CASH -> {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = amountPaidStr,
                                        onValueChange = { amountPaidStr = it },
                                        label = { Text("Valor Entregue (Kz)") },
                                        modifier = Modifier.weight(1f).testTag("cash_amount_input"),
                                        singleLine = true
                                    )
                                    Surface(
                                        modifier = Modifier.weight(1f).height(56.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(text = "Troco Calculado:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "${Medication.formatKz(change)} Kz",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (change > 0) Color(0xFF047857) else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                            PaymentMethod.TPA_MULTICAIXA -> {
                                OutlinedTextField(
                                    value = referenceStr,
                                    onValueChange = { referenceStr = it },
                                    label = { Text("Nº de Autorização / Talão POS Multicaixa") },
                                    placeholder = { Text("Ex: TPA-99214") },
                                    modifier = Modifier.fillMaxWidth().testTag("tpa_reference_input"),
                                    singleLine = true
                                )
                            }
                            PaymentMethod.MULTICAIXA_EXPRESS -> {
                                OutlinedTextField(
                                    value = referenceStr,
                                    onValueChange = { referenceStr = it },
                                    label = { Text("Nº Telefone MCX Express (+244)") },
                                    placeholder = { Text("Ex: 923 456 789") },
                                    modifier = Modifier.fillMaxWidth().testTag("mcx_phone_input"),
                                    singleLine = true
                                )
                            }
                            PaymentMethod.HEALTH_INSURANCE -> {
                                OutlinedTextField(
                                    value = referenceStr,
                                    onValueChange = { referenceStr = it },
                                    label = { Text("Nº do Termo de Responsabilidade / Cartão") },
                                    placeholder = { Text("Ex: ENSA-AP-88421") },
                                    modifier = Modifier.fillMaxWidth().testTag("insurance_ref_input"),
                                    singleLine = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Botão de Emitir Factura-Recibo e Gerar Impressão
                        Button(
                            onClick = {
                                onProcessPayment(
                                    order.id,
                                    selectedPaymentMethod,
                                    amountPaid,
                                    referenceStr
                                )
                                referenceStr = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("emit_invoice_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emitir Factura-Recibo & Imprimir Talão", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Seção: Histórico de Vendas Concluídas do Turno
        item {
            Text(
                text = "Facturas Emitidas no Turno (${completedSales.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        items(completedSales, key = { it.id }) { sale ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = sale.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                text = sale.paymentMethod.displayName,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(text = "${sale.patientName} (${sale.patientBi})", fontSize = 11.sp)
                        Text(
                            text = "${SimpleDateFormat("HH:mm", Locale.US).format(Date(sale.timestamp))} • Total: ${sale.totalFormattedKz}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { onViewReceipt(sale) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("view_receipt_btn_${sale.id}")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver Talão", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // Modal de Resumo de Fecho do Turno
    if (showShiftReportModal) {
        Dialog(onDismissRequest = { showShiftReportModal = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Fecho de Caixa do Turno", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showShiftReportModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Text(text = "Data: ${shiftSummary.shiftDate} • Operador: ${shiftSummary.cashierName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Volume Total Faturado", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = shiftSummary.totalTurnoverFormattedKz,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(text = "${shiftSummary.totalTransactions} transações efetuadas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Discriminação por Meio de Pagamento:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    ShiftSummaryRow("Numerário (Kz em Caixa):", "${Medication.formatKz(shiftSummary.totalCashKz)} Kz")
                    ShiftSummaryRow("TPA Multicaixa:", "${Medication.formatKz(shiftSummary.totalTpaKz)} Kz")
                    ShiftSummaryRow("Multicaixa Express:", "${Medication.formatKz(shiftSummary.totalMcxKz)} Kz")
                    ShiftSummaryRow("Seguradoras (A Receber):", "${Medication.formatKz(shiftSummary.totalInsuranceKz)} Kz")

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { showShiftReportModal = false },
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Imprimir Relatório de Turno e Concluir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShiftSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
