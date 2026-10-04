package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppRole(
    val title: String,
    val shortName: String,
    val activeUser: String,
    val userRoleTitle: String,
    val icon: ImageVector
) {
    RECEPTION(
        title = "Recepção e Triagem",
        shortName = "Recepção",
        activeUser = "Enf. Carlos Benguela",
        userRoleTitle = "Triagem & Admissão",
        icon = Icons.Default.Assignment
    ),
    DOCTOR(
        title = "Consultório Médico (PEP)",
        shortName = "Consultório",
        activeUser = "Dra. Luísa Kiala",
        userRoleTitle = "Clínica Geral & Medicina Tropical",
        icon = Icons.Default.MedicalServices
    ),
    PHARMACY(
        title = "Farmácia / PDV",
        shortName = "Farmácia",
        activeUser = "Farm. Paulo Gaspar",
        userRoleTitle = "Farmacêutico Chefe",
        icon = Icons.Default.LocalPharmacy
    ),
    CASHIER(
        title = "Gestão / Caixa",
        shortName = "Caixa",
        activeUser = "Teresa Gomes",
        userRoleTitle = "Operadora de Caixa e Facturação",
        icon = Icons.Default.PointOfSale
    )
}
