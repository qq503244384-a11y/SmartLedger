package com.example.smartledger.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BudgetBar(spent: Double, budget: Double) {
    val ratio = if (budget > 0) (spent / budget).coerceAtMost(1.5) else 0.0
    val color = if (budget > 0 && spent > budget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small)
            .padding(end = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(ratio.toFloat())
                .height(16.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
    }
    Text(
        "已用 ${"%.2f".format(spent)} / 预算 ${"%.2f".format(budget)}",
        style = MaterialTheme.typography.bodySmall,
        color = if (budget > 0 && spent > budget) MaterialTheme.colorScheme.error else Color.Unspecified
    )
}






