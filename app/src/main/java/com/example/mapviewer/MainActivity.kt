package com.example.mapviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mapviewer.ui.theme.MapViewerTheme

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.inputmethodservice.Keyboard
import android.util.Log
import androidx.annotation.Nullable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.io.FileOutputStream

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Blob
import kotlin.math.*
import kotlin.system.measureNanoTime
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.ui.focus.onFocusChanged


import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.PopupProperties


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        setContent {
            MapViewerTheme {


                var tileMap by remember { mutableStateOf(TileMap(10, -2650.0, -1100.0, this)) }
                var pins by remember {mutableStateOf(Pins())}


                DraggableMap(this, parentTileMap = tileMap, onTileMapChange = { tileMap = it }, pins = pins)
                //DraggableMap(this)


                Overlay(pins = pins)

                
            }
        }
    }
}


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

