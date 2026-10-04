package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BPStatus
import com.example.data.model.PatientStatus
import com.example.data.model.PrescriptionStatus
import com.example.data.model.StockAlertLevel

@Composable
fun PatientStatusBadge(status: PatientStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status) {
        PatientStatus.WAITING_TRIAGE -> Triple(
            Color(0xFFFEF3C7),
            Color(0xFF92400E),
            Icons.Default.HourglassEmpty
        )
        PatientStatus.WAITING_CONSULTATION -> Triple(
            Color(0xFFDBEAFE),
            Color(0xFF1E40AF),
            Icons.Default.MedicalServices
        )
        PatientStatus.IN_CONSULTATION -> Triple(
            Color(0xFFE0E7FF),
            Color(0xFF4338CA),
            Icons.Default.PlayCircleFilled
        )
        PatientStatus.COMPLETED -> Triple(
            Color(0xFFD1FAE5),
            Color(0xFF065F46),
            Icons.Default.CheckCircle
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = textColor
        )
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StockAlertBadge(level: StockAlertLevel, daysLeft: Int = 0, stockQty: Int = 0, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon, label) = when (level) {
        StockAlertLevel.EXPIRING_SOON -> Triple(
            Color(0xFFFFEDD5),
            Color(0xFF9A3412),
            Icons.Default.Warning
        ).let { (b, t, i) ->
            Quad(b, t, i, if (daysLeft > 0) "Vence em $daysLeft dias (Atenção)" else "Vence em breve")
        }
        StockAlertLevel.LOW_STOCK -> Quad(
            Color(0xFFFEF9C3),
            Color(0xFF854D0E),
            Icons.Default.TrendingDown,
            "Estoque Baixo ($stockQty un)"
        )
        StockAlertLevel.OUT_OF_STOCK -> Quad(
            Color(0xFFFEE2E2),
            Color(0xFF991B1B),
            Icons.Default.Cancel,
            "Esgotado (0 un)"
        )
        StockAlertLevel.NORMAL -> Quad(
            Color(0xFFECFDF5),
            Color(0xFF047857),
            Icons.Default.CheckCircle,
            "Regular ($stockQty un)"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = textColor
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun BPStatusBadge(status: BPStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        BPStatus.NORMAL -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        BPStatus.PRE_HYPERTENSION -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        BPStatus.STAGE_1_HYPERTENSION -> Color(0xFFFFEDD5) to Color(0xFF9A3412)
        BPStatus.STAGE_2_HYPERTENSION -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        BPStatus.HYPOTENSION -> Color(0xFFE0E7FF) to Color(0xFF3730A3)
    }

    Text(
        text = status.label,
        color = textColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun PrescriptionStatusBadge(status: PrescriptionStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        PrescriptionStatus.PENDING_DISPENSE -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        PrescriptionStatus.DISPENSED -> Color(0xFFD1FAE5) to Color(0xFF047857)
        PrescriptionStatus.CANCELLED -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
    }

    Text(
        text = status.label,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
