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
import com.example.ui.components.PrescriptionStatusBadge
import com.example.ui.components.StockAlertBadge

@Composable
fun PharmacyScreen(
    medications: List<Medication>,
    prescriptions: List<Prescription>,
    onDispensePrescription: (String) -> Unit,
    onSubmitDirectSale: (customerName: String, customerBi: String, coverage: String, items: List<CartItem>) -> Unit,
    onOpenRestock: (Medication) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPharmacyTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Prescrições Médicas", "Venda de Balcão (PDV)", "Inventário & Alertas")

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Row da Farmácia
        TabRow(
            selectedTabIndex = selectedPharmacyTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.secondary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedPharmacyTab == index,
                    onClick = { selectedPharmacyTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedPharmacyTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        when (selectedPharmacyTab) {
            0 -> PrescriptionDispensingTab(
                prescriptions = prescriptions,
                medications = medications,
                onDispense = onDispensePrescription
            )
            1 -> DirectSalePosTab(
                medications = medications,
                onCheckout = onSubmitDirectSale
            )
            2 -> InventoryAlertsTab(
                medications = medications,
                onRestock = onOpenRestock
            )
        }
    }
}

// -------------------------------------------------------------
// ABA 1: ATENDIMENTO DE PRESCRIÇÕES DIGITAIS (FEFO)
// -------------------------------------------------------------
@Composable
fun PrescriptionDispensingTab(
    prescriptions: List<Prescription>,
    medications: List<Medication>,
    onDispense: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPrescription by remember { mutableStateOf<Prescription?>(null) }

    val filtered = prescriptions.filter {
        it.code.contains(searchQuery, ignoreCase = true) ||
                it.patientName.contains(searchQuery, ignoreCase = true) ||
                it.patientBi.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Dispensação de Receitas Digitais",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Busca rápida por Código de Receita ou Nome do Paciente",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Código da Receita (ex: REC-2026-084) ou Nome...") },
                leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("prescription_search_input"),
                singleLine = true
            )
        }

        // Se uma receita foi carregada com 1-clique
        if (selectedPrescription != null) {
            val pres = selectedPrescription!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = pres.code,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF166534)
                                    )
                                    PrescriptionStatusBadge(status = pres.status)
                                }
                                Text(
                                    text = "Paciente: ${pres.patientName} (${pres.patientBi})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Médico: ${pres.doctorName} • Dx: ${pres.diagnosis}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { selectedPrescription = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Itens da Prescrição Carregados (Aplicação FEFO no estoque):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        pres.items.forEach { item ->
                            val currentMed = medications.find { it.id == item.medicationId }
                            val isAvailable = currentMed != null && currentMed.stockQuantity >= item.quantity

                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${item.quantity}x ${item.tradeName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "${item.dosage} • ${item.frequency} • ${item.duration}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (currentMed != null) {
                                            Text(
                                                text = "Lote mais próximo (FEFO): ${currentMed.batchNumber} (Val: ${currentMed.expirationDate})",
                                                fontSize = 9.5.sp,
                                                color = Color(0xFF0D9488)
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = item.subtotalFormattedKz,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = if (isAvailable) "Disponível (${currentMed?.stockQuantity} un)" else "Sem estoque suficiente",
                                            fontSize = 9.sp,
                                            color = if (isAvailable) Color(0xFF059669) else Color(0xFFDC2626),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total a Faturar:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = pres.totalFormattedKz,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (pres.status == PrescriptionStatus.PENDING_DISPENSE) {
                            Button(
                                onClick = {
                                    onDispense(pres.code)
                                    selectedPrescription = null
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("dispense_prescription_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dispensar com FEFO e Enviar ao Caixa", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Text(
                                text = "Receita já dispensada e encaminhada ao Caixa.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }
        }

        // Lista de Receitas na Fila
        item {
            Text(
                text = "Fila de Prescrições Recebidas (${filtered.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        items(filtered, key = { it.code }) { pres ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedPrescription = pres }
                    .testTag("prescription_card_${pres.code}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = pres.code, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            PrescriptionStatusBadge(status = pres.status)
                        }
                        Text(
                            text = "${pres.patientName} (${pres.patientBi})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${pres.items.size} medicamento(s) • Total: ${pres.totalFormattedKz}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { selectedPrescription = pres },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Carregar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ABA 2: VENDA DIRETA DE BALCÃO (PDV)
// -------------------------------------------------------------
@Composable
fun DirectSalePosTab(
    medications: List<Medication>,
    onCheckout: (customerName: String, customerBi: String, coverage: String, items: List<CartItem>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("Consumidor Final") }
    var customerBi by remember { mutableStateOf("999999999LA000") }
    var selectedCoverage by remember { mutableStateOf("Particular") }

    val cart = remember { mutableStateListOf<CartItem>() }

    val filteredMeds = medications.filter {
        it.tradeName.contains(searchQuery, ignoreCase = true) ||
                it.genericName.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.barcode.contains(searchQuery)
    }

    val cartTotal = cart.sumOf { it.subtotalKz }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Ponto de Venda de Balcão (PDV Farmácia)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Dados do Cliente
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Cliente / Utente") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = customerBi,
                            onValueChange = { customerBi = it },
                            label = { Text("BI / NIF") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Resumo do Carrinho
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (cart.isEmpty()) MaterialTheme.colorScheme.surface else Color(0xFFF0FDF4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Carrinho de Venda (${cart.size} itens)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${Medication.formatKz(cartTotal)} Kz",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (cart.isEmpty()) {
                        Text(
                            text = "Nenhum produto adicionado. Pesquise abaixo e clique para adicionar.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        cart.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.tradeName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${item.quantity} x ${Medication.formatKz(item.unitPriceKz)} Kz (Lote: ${item.batchNumber})",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.subtotalFormattedKz,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { cart.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remover", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onCheckout(customerName, customerBi, selectedCoverage, cart.toList())
                                cart.clear()
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("checkout_pos_sale_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Concluir e Enviar para Facturação no Caixa", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Pesquisa de Produtos do Balcão
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar no Catálogo Farmacêutico...") },
                placeholder = { Text("Nome, Genérico ou Código de Barras") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("pos_product_search_input"),
                singleLine = true
            )
        }

        items(filteredMeds, key = { it.id }) { med ->
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
                        Text(text = med.tradeName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${med.genericName} • ${med.dosageForm}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = med.priceFormattedKz, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Lote: ${med.batchNumber}", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text(text = "Estoque: ${med.stockQuantity} un", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = {
                            val existingIndex = cart.indexOfFirst { it.medicationId == med.id }
                            if (existingIndex != -1) {
                                val current = cart[existingIndex]
                                if (current.quantity < med.stockQuantity) {
                                    cart[existingIndex] = current.copy(quantity = current.quantity + 1)
                                }
                            } else {
                                if (med.stockQuantity > 0) {
                                    cart.add(
                                        CartItem(
                                            medicationId = med.id,
                                            tradeName = med.tradeName,
                                            genericName = med.genericName,
                                            batchNumber = med.batchNumber,
                                            unitPriceKz = med.unitPriceKz,
                                            quantity = 1
                                        )
                                    )
                                }
                            }
                        },
                        enabled = !med.isOutOfStock,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_to_cart_btn_${med.id}")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (med.isOutOfStock) "Sem Estoque" else "+ Vender", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ABA 3: INVENTÁRIO, ALERTAS DE VALIDADE & CONTROLO DE LOTE
// -------------------------------------------------------------
@Composable
fun InventoryAlertsTab(
    medications: List<Medication>,
    onRestock: (Medication) -> Unit
) {
    var filterLevel by remember { mutableStateOf<StockAlertLevel?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = medications.filter { med ->
        val matchesQuery = med.tradeName.contains(searchQuery, ignoreCase = true) ||
                med.genericName.contains(searchQuery, ignoreCase = true) ||
                med.batchNumber.contains(searchQuery, ignoreCase = true)
        val matchesAlert = filterLevel == null || med.alertLevel == filterLevel
        matchesQuery && matchesAlert
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Inventário Farmacêutico & Alertas de Validade",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Controle de lotes, datas de expiração e reposição de estoque",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Filtros Rápidos de Alerta
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = filterLevel == null,
                    onClick = { filterLevel = null },
                    label = { Text("Todos (${medications.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = filterLevel == StockAlertLevel.EXPIRING_SOON,
                    onClick = { filterLevel = if (filterLevel == StockAlertLevel.EXPIRING_SOON) null else StockAlertLevel.EXPIRING_SOON },
                    label = { Text("Validade (< 30 dias)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFEDD5),
                        selectedLabelColor = Color(0xFF9A3412)
                    )
                )
                FilterChip(
                    selected = filterLevel == StockAlertLevel.LOW_STOCK,
                    onClick = { filterLevel = if (filterLevel == StockAlertLevel.LOW_STOCK) null else StockAlertLevel.LOW_STOCK },
                    label = { Text("Estoque Baixo", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFEF9C3),
                        selectedLabelColor = Color(0xFF854D0E)
                    )
                )
            }
        }

        // Tabela / Cards de Medicamentos com Badges
        items(filtered, key = { it.id }) { med ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = med.tradeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${med.genericName} • ${med.dosageForm}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        StockAlertBadge(
                            level = med.alertLevel,
                            daysLeft = med.daysUntilExpiry,
                            stockQty = med.stockQuantity
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Lote: ${med.batchNumber} • Vence em: ${med.expirationDate}", fontSize = 11.sp, color = Color(0xFF475569))
                            Text(text = "Preço Unitário: ${med.priceFormattedKz}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        OutlinedButton(
                            onClick = { onRestock(med) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("restock_btn_${med.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Entrada", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
