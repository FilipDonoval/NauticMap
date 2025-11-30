package com.example.mapviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.round

@Composable
fun Overlay(pins: Pins) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        TopBar(
            modifier = Modifier.align(Alignment.TopStart),
            pins = pins
        )

        BottomBar(
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}


@Composable
fun TopBar(modifier: Modifier = Modifier, pins: Pins) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 250.dp)
            .background(color = Color(255, 255, 255, 200))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 25.dp)
        )
        {
            Text(
                text = "Distance: ${round(pins.calculateDistance() * 100) / 100} knots"
            )
            Row()
            {
                Button(
                    modifier = Modifier
                        .height(50.dp),
                    onClick = {
                        pins.clearPins()
                    }
                ) { Text("Clear")}
                Button(
                    modifier = Modifier
                        .height(50.dp),
                    onClick = {
                        pins.popPin()
                    }
                ) { Text("Undo")}
            }

        }
    }
}

@Composable
fun BottomBar(modifier: Modifier = Modifier)
{
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 250.dp)
            .background(Color(200, 200, 200, 200))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    )
}