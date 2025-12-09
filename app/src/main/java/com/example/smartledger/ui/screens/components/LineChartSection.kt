package com.example.smartledger.ui.screens.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun LineChartSection(
    points: List<Pair<Int, Double>>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description = Description().apply { text = "" }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = points.map { Entry(it.first.toFloat(), it.second.toFloat()) }
            val dataSet = LineDataSet(entries, "趋势").apply {
                color = Color.parseColor("#4F46E5")
                valueTextColor = Color.DKGRAY
                setDrawCircles(true)
                setCircleColor(Color.parseColor("#14B8A6"))
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}


