package com.example.graphics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun LineGraph(
    points: List<Float>,
    xStep: Int,
    lineWidth: Float,
    lineColor: Color,
    modifier: Modifier = Modifier,
    graphicModifier: Modifier = Modifier,
    textBlockModifier: Modifier = Modifier,
    showMinMaxValue: Boolean = false,
    textColor: Color = Color.White,
) {
    var widthInDp by remember { mutableStateOf(0.dp) }
    var heightInDp by remember { mutableStateOf(0.dp) }

    var newPointsValue = points.takeLast((widthInDp.value / xStep).toInt())

    val minValue = newPointsValue.minOrNull()
    val maxValue = newPointsValue.maxOrNull()

    Box(
        modifier = modifier,
    ) {
        Box(
            modifier = graphicModifier
                .onGloballyPositioned { layoutCoordinates ->
                    widthInDp = with(layoutCoordinates.size.width) { this.toFloat().dp }
                    heightInDp = with(layoutCoordinates.size.height) { this.toFloat().dp }
                },
        ) {
            if (newPointsValue.isEmpty() || minValue == null || maxValue == null) {
                return
            }
            newPointsValue =
                if (maxValue == minValue) {
                    newPointsValue.map { heightInDp.value }
                } else {
                    newPointsValue.map { heightInDp.value * (1 - (it - minValue) / (maxValue - minValue)) }
                }

            val path = Path()

            path.moveTo(0f, newPointsValue.first().toFloat())

            newPointsValue.forEachIndexed { index, value ->
                if (index == 0) {
                    return@forEachIndexed
                }
                path.lineTo((index * xStep).toFloat(), value)
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = lineWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
        if (showMinMaxValue) {
            Column(
                modifier = textBlockModifier
            ) {
                Text("$maxValue", color = textColor)
                Text("$minValue", color = textColor)
            }
        }
    }
}

@Preview
@Composable
private fun LineGraphPreview() {
    val minYOffset = 0
    val maxYOffset = 100
    val delay = 1L

    var points by remember {
        mutableStateOf(
            mutableListOf<Float>().apply {
                repeat(100) {
                    add((minYOffset..maxYOffset).random().toFloat())
                }
            }.toList(),
        )
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(delay)
            points = points.toMutableList().apply {
                add(
                    (points.lastOrNull() ?: 0f) +
                        (-1..1).random() *
                        (minYOffset..maxYOffset).random().toFloat(),
                )
            }
        }
    }
    LineGraph(
        points = points,
        xStep = 10,
        lineWidth = 5f,
        lineColor = Color(0xFFFD6C0C),
        showMinMaxValue = true,
        graphicModifier = Modifier
            .height(50.dp)
            .fillMaxWidth(),
        modifier = Modifier
            .background(Color.Black),
    )
}
