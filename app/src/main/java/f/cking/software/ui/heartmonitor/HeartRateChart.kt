package f.cking.software.ui.heartmonitor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import f.cking.software.domain.model.HeartRateReading
import kotlin.math.max
import kotlin.math.min

@Composable
fun HeartRateLineChart(
    readings: List<HeartRateReading>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    showGrid: Boolean = true,
    showLabels: Boolean = true
) {
    if (readings.isEmpty()) {
        return
    }

    val sortedReadings = readings.sortedBy { it.timestamp }
    val minHeartRate = sortedReadings.minOfOrNull { it.heartRate } ?: 40
    val maxHeartRate = sortedReadings.maxOfOrNull { it.heartRate } ?: 200

    // Add some padding to the range
    val rangeMin = max(30, minHeartRate - 10)
    val rangeMax = min(220, maxHeartRate + 10)
    val rangeSpan = rangeMax - rangeMin

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 40f

        // Draw grid lines if enabled
        if (showGrid) {
            val gridColor = Color.Gray.copy(alpha = 0.2f)
            val gridStroke = Stroke(width = 1f)

            // Horizontal grid lines
            for (i in 0..5) {
                val y = padding + (height - 2 * padding) * i / 5
                drawLine(
                    color = gridColor,
                    start = Offset(padding, y),
                    end = Offset(width - padding, y),
                    strokeWidth = gridStroke.width
                )
            }

            // Vertical grid lines
            for (i in 0..5) {
                val x = padding + (width - 2 * padding) * i / 5
                drawLine(
                    color = gridColor,
                    start = Offset(x, padding),
                    end = Offset(x, height - padding),
                    strokeWidth = gridStroke.width
                )
            }
        }

        // Draw labels if enabled
        if (showLabels) {
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 30f
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            // Y-axis labels (heart rate values)
            for (i in 0..5) {
                val heartRate = rangeMax - (rangeSpan * i / 5)
                val y = padding + (height - 2 * padding) * i / 5
                drawContext.canvas.nativeCanvas.drawText(
                    "${heartRate}",
                    padding - 10f,
                    y + 10f,
                    textPaint
                )
            }
        }

        // Draw the heart rate line
        if (sortedReadings.size > 1) {
            val path = Path()
            val firstReading = sortedReadings.first()
            val lastReading = sortedReadings.last()
            val timeSpan = (lastReading.timestamp - firstReading.timestamp).toFloat()

            sortedReadings.forEachIndexed { index, reading ->
                val x = padding + (width - 2 * padding) *
                        ((reading.timestamp - firstReading.timestamp).toFloat() / timeSpan)
                val normalizedValue = (reading.heartRate - rangeMin).toFloat() / rangeSpan
                val y = height - padding - (height - 2 * padding) * normalizedValue

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 4f)
            )

            // Draw points
            sortedReadings.forEach { reading ->
                val x = padding + (width - 2 * padding) *
                        ((reading.timestamp - firstReading.timestamp).toFloat() / timeSpan)
                val normalizedValue = (reading.heartRate - rangeMin).toFloat() / rangeSpan
                val y = height - padding - (height - 2 * padding) * normalizedValue

                drawCircle(
                    color = lineColor,
                    radius = 6f,
                    center = Offset(x, y)
                )
            }
        }
    }
}
