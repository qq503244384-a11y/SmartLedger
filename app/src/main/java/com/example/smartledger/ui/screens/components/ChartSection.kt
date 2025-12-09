package com.example.smartledger.ui.screens.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

@Composable
fun PieChartSection(
    data: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setUsePercentValues(false)
                setEntryLabelColor(Color.WHITE)
            }
        },
        update = { chart ->
            val entries = data.entries
                .filter { it.value > 0 }
                .map { PieEntry(it.value.toFloat(), it.key) }
            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(
                    Color.parseColor("#4F46E5"),
                    Color.parseColor("#14B8A6"),
                    Color.parseColor("#F59E0B"),
                    Color.parseColor("#EF4444"),
                    Color.parseColor("#6366F1")
                )
                valueTextColor = Color.WHITE
            }
            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}


