package com.example.graphics

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.graphics.ui.LineGraph
import com.example.graphics.ui.SumLineGraph
import kotlinx.coroutines.delay
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isLandscape = LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE

            val minYOffset = 0
            val maxYOffset = 100
            val delay = 5L

            val areaColors = listOf(
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

            val singleGraphMode = remember {
                mutableStateOf(false)
            }

            var numOfGraph by remember {
                mutableIntStateOf(10)
            }

            var pointsGroup by remember {
                mutableStateOf(listOf<List<Float>>())
            }

            var points by remember {
                mutableStateOf(listOf<Float>())
            }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(delay)

                    val outerList = mutableListOf<List<Float>>()
                    repeat(numOfGraph) {
                        val innerList = (pointsGroup.getOrNull(it)?.toMutableList() ?: mutableListOf()).apply {
                            add(
                                (pointsGroup.getOrNull(it)?.lastOrNull() ?: 0f) +
                                    (-1..1).random() *
                                        (minYOffset..maxYOffset).random().toFloat(),
                            )
                        }
                        outerList.add(innerList.map { value -> abs(value) }.toList())
                    }
                    pointsGroup = outerList.toList()
                    points = pointsGroup[0].toList()
                }
            }
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .clickable {
                        singleGraphMode.value = !singleGraphMode.value
                    }
                    .fillMaxSize()
            ) {
                if (singleGraphMode.value) {
                    LineGraph(
                        points = points,
                        xStep = 3,
                        lineWidth = 5f,
                        lineColor = areaColors[0],
                        showMinMaxValue = true,
                        graphicModifier = Modifier
                            .padding(top = 40.dp, end = if (isLandscape) 30.dp else 5.dp)
                            .fillMaxSize(),
                        textBlockModifier = Modifier
                            .padding(top = 40.dp, start = 5.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 40.dp, end = 7.dp)
                            .background(Color.Gray)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .clickable(enabled = numOfGraph > 1) {
                                    numOfGraph--
                                }
                                .padding(10.dp)
                        )
                        Text(
                            text = "$numOfGraph",
                            color = Color.White,
                            modifier = Modifier
                                .padding(10.dp)
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .clickable(enabled = numOfGraph < areaColors.size) {
                                    numOfGraph++
                                }
                                .padding(10.dp)
                        )
                    }
                    SumLineGraph(
                        pointsGroup = pointsGroup,
                        xStep = 3,
                        lineWidth = 2f,
                        showMaxValue = true,
                        graphicModifier = Modifier
                            .padding(top = 40.dp, end = if (isLandscape) 30.dp else 5.dp)
                            .fillMaxSize(),
                        textBlockModifier = Modifier
                            .padding(top = 40.dp, start = 5.dp),
                        areaColors = areaColors
                    )
                }
            }
        }
    }
}
