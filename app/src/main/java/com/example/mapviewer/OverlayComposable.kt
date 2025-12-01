package com.example.mapviewer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.contracts.contract
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
            modifier = Modifier.align(Alignment.BottomStart),
            pins = pins
        )
    }
}


@Composable
fun TopBar(modifier: Modifier = Modifier, pins: Pins) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 150.dp)
            .background(color = Color(255, 255, 255, 100))
            .pointerInput(Unit) {
                /*awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }*/
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
                text = "Distance: ${round(pins.calculateDistance() * 100) / 100} knots",
                fontSize = 16.sp
            )
            /*Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.End)
            )
            {
                Button(
                    modifier = Modifier
                        .height(48.dp),
                    onClick = {
                        pins.clearPins()
                        Log.d("dkljsadasklj", "Clear")
                    },
                    contentPadding = PaddingValues(8.dp, end = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.CenterHorizontally)
                    )
                    {
                        Icon(
                            painter = painterResource(R.drawable.delete_24dp),
                            contentDescription = null
                        )
                        Text(text = "Clear", fontSize = 16.sp)
                    }
                }

                Button(
                    modifier = Modifier
                        .height(48.dp),
                    onClick = {
                        pins.popPin()
                        Log.d("dkljsadasklj", "Undo")
                    },
                    contentPadding = PaddingValues(8.dp, end = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.CenterHorizontally)
                    )
                    {
                        Icon(
                            painter = painterResource(R.drawable.undo_24dp),
                            contentDescription = null
                        )
                        Text(text = "Undo", fontSize = 16.sp)
                    }

                }
            }*/
        }
    }
}

@Composable
fun BottomBar(modifier: Modifier = Modifier, pins: Pins)
{
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 250.dp)
            .background(Color(200, 200, 200, 100))
            .pointerInput(Unit) {
                /*awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }*/
            }
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp)
        )
        {
            /*Text(
                text = "Distance: ${round(pins.calculateDistance() * 100) / 100} knots",
                fontSize = 16.sp
            )*/
            Spacer(modifier = Modifier.weight(1f))
            /*
            Button(
                onClick = {
                    pins.placementActive = !pins.placementActive
                    Log.d("dsajkllkjdas", "active/deactive: ${pins.placementActive}")
                }

            ){}
            Checkbox(
                checked = pins.placementActive.value,
                onCheckedChange = {
                    pins.placementActive.value = it
                    Log.d("dsajkllkjdas", "checkbox: ${pins.placementActive.value}")
                }
            )

            IconButton(
                onClick = {
                    pins.placementActive.value = !pins.placementActive.value
                    Log.d("dsajkllkjdas", "checkbox: ${pins.placementActive.value}")
                }
            )
            {
                Icon(
                    painter = if (pins.placementActive.value) painterResource(R.drawable.conversion_path_24dp) else painterResource(R.drawable.delete_24dp),
                    contentDescription = null
                )
            }*/

            Button(
                modifier = Modifier
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pins.placementActive.value) Color(ButtonDefaults.buttonColors().containerColor.red * 0.5f, ButtonDefaults.buttonColors().containerColor.green * 0.5f, ButtonDefaults.buttonColors().containerColor.blue * 0.5f)
                    else ButtonDefaults.buttonColors().containerColor,
                    contentColor = ButtonDefaults.buttonColors().contentColor
                ),
                contentPadding = PaddingValues(8.dp, end = 12.dp),
                onClick = {
                    pins.placementActive.value = !pins.placementActive.value
                    Log.d("dsajkllkjdas", "checkbox: ${pins.placementActive.value}")
                }
            )
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.CenterHorizontally)
                )
                {
                    Icon(
                        painter = painterResource(R.drawable.conversion_path_24dp),
                        contentDescription = null
                    )
                    Text(text = "Path", fontSize = 16.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.End)
            )
            {
                Button(
                    modifier = Modifier
                        .height(48.dp),
                    onClick = {
                        pins.clearPins()
                        Log.d("dkljsadasklj", "Clear")
                    },
                    contentPadding = PaddingValues(8.dp, end = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.CenterHorizontally)
                    )
                    {
                        Icon(
                            painter = painterResource(R.drawable.delete_24dp),
                            contentDescription = null
                        )
                        Text(text = "Clear", fontSize = 16.sp)
                    }
                }

                Button(
                    modifier = Modifier
                        .height(48.dp),
                    onClick = {
                        pins.popPin()
                        Log.d("dkljsadasklj", "Undo")
                    },
                    contentPadding = PaddingValues(8.dp, end = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.CenterHorizontally)
                    )
                    {
                        Icon(
                            painter = painterResource(R.drawable.undo_24dp),
                            contentDescription = null
                        )
                        Text(text = "Undo", fontSize = 16.sp)
                    }

                }
            }
        }
    }
}