package com.example.graphics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs

// Количество графиков должно быть не меньше количества цветов
// У точек должны быть не отрицательные значения
@Composable
internal fun SumLineGraph(
    pointsGroup: List<List<Float>>,
    xStep: Int,
    lineWidth: Float,
    modifier: Modifier = Modifier,
    graphicModifier: Modifier = Modifier,
    showMaxValue: Boolean = false,
    textColor: Color = Color.White,
    textBlockModifier: Modifier = Modifier,
    areaColors: List<Color> = defaultAreaColors,
) {
    if (pointsGroup.size > areaColors.size) {
        throw IllegalArgumentException("pointsGroup.size must be less or equal areaColors.size (${areaColors.size})")
    }
    if (pointsGroup.any { points -> points.any { pointValue -> pointValue < 0f } }) {
        throw IllegalArgumentException("Values of the points must be non-negative")
    }

    var widthInDp by remember { mutableStateOf(0.dp) }
    var heightInDp by remember { mutableStateOf(0.dp) }

    val actualPointsGroup = pointsGroup.map { it.takeLast((widthInDp.value / xStep).toInt()) }

    var cumulativeSum: List<Float>
    var formatedPointsGroup: MutableList<List<Float>> = mutableListOf()

    var maxValue: Float? = null

    if (actualPointsGroup.isNotEmpty()) {
        cumulativeSum = List(actualPointsGroup[0].size) { 0.0f }

        for (points in actualPointsGroup) {
            cumulativeSum = cumulativeSum.zip(points) { a, b -> a + b }
            formatedPointsGroup.add(cumulativeSum)
        }
        maxValue = formatedPointsGroup.lastOrNull()?.maxOrNull()
    }

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
            if (actualPointsGroup.isEmpty() || maxValue == null) {
                return
            }

            var lastPathPoints = listOf(
                Pair(0f, heightInDp.value),
                Pair((formatedPointsGroup[0].size * xStep).toFloat(), heightInDp.value),
            )

            formatedPointsGroup = formatedPointsGroup.map { points ->
                points.map { value ->
                    if (maxValue == 0f) {
                        heightInDp.value
                    } else {
                        heightInDp.value * (1 - value / maxValue)
                    }
                }
            }.toMutableList()

            repeat(pointsGroup.size) { groupIndex ->
                val thisPathPoints = formatedPointsGroup[groupIndex].mapIndexed { index, value ->
                    Pair((index * xStep).toFloat(), value)
                }
                val areaPath = listToPath(
                    thisPathPoints + lastPathPoints.take(thisPathPoints.size).reversed(),
                )
                lastPathPoints = thisPathPoints

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawPath(
                        path = areaPath,
                        color = areaColors[groupIndex].copy(alpha = 0.5f),
                        style = Fill,
                    )
                    drawPath(
                        path = listToPath(thisPathPoints),
                        color = areaColors[groupIndex],
                        style = Stroke(
                            width = lineWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }
            }
        }
        if (showMaxValue) {
            Column(
                modifier = textBlockModifier
            ) {
                Text("$maxValue", color = textColor)
            }
        }
    }
}

private fun listToPath(points: List<Pair<Float, Float>>): Path {
    if (points.isEmpty()) {
        return Path()
    }
    val path = Path().apply {
        moveTo(points.first().first, points.first().second)
        points.forEachIndexed { index, point ->
            if (index == 0) {
                return@forEachIndexed
            }
            lineTo(point.first, point.second)
        }
    }
    return path
}

private val defaultAreaColors = listOf(
    Color(0xFFFD6C0C),
    Color(0xFF8D0CFD),
    Color(0xFFB0FF1D),
    Color(0xFFB8695B),
    Color(0xFFFDF10C),
    Color(0xFF0CFDB9),
    Color(0xFFFD0CDD),
    Color(0xFF0C8DFD),
    Color(0xFFFD0C58),
    Color(0xFFFFF9F9),
)

@Preview(
    widthDp = 1100,
    heightDp = 700
)
@Composable
private fun SumLineGraphPreview() {
    val minYOffset = 0
    val maxYOffset = 100
    val delay = 1L
    val numOfGraph = 4

    var pointsGroup by remember {
        mutableStateOf(listOf<List<Float>>(
            mutableListOf<Float>().apply {
                repeat(100) {
                    add((minYOffset..maxYOffset).random().toFloat())
                }
            },
            mutableListOf<Float>().apply {
                repeat(100) {
                    add((minYOffset..maxYOffset).random().toFloat())
                }
            },
            mutableListOf<Float>().apply {
                repeat(100) {
                    add((minYOffset..maxYOffset).random().toFloat())
                }
            },
            mutableListOf<Float>().apply {
                repeat(100) {
                    add((minYOffset..maxYOffset).random().toFloat())
                }
            },
        ))
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(delay)

            val outerList = mutableListOf<List<Float>>()
            repeat(numOfGraph) {
                val innerList = (pointsGroup.getOrNull(it)?.toMutableList() ?: mutableListOf()).apply {
                    add(
                        abs(
                            (pointsGroup.getOrNull(it)?.lastOrNull() ?: 0f) +
                                (-1..1).random() *
                                (minYOffset..maxYOffset).random().toFloat(),
                        ),
                    )
                }
                outerList.add(innerList.toList())
            }
            pointsGroup = outerList.toList()
        }
    }
    Box(
        modifier = Modifier
            .background(Color.Black)
            .padding(25.dp),
    ) {
        SumLineGraph(
            pointsGroup = pointsGroup,
            xStep = 20,
            lineWidth = 3f,
            showMaxValue = true,
            graphicModifier = Modifier
                .fillMaxSize(),
            areaColors = listOf(
                Color(0xFFFD6C0C),
                Color(0xFF8D0CFD),
                Color(0xFFB0FF1D),
                Color(0xFFB8695B),
                Color(0xFFFDF10C),
                Color(0xFF0CFDB9),
                Color(0xFFFD0CDD),
                Color(0xFF0C8DFD),
                Color(0xFFFD0C58),
                Color(0xFFFFF9F9),
                Color.Magenta,
            )
        )
    }
}
