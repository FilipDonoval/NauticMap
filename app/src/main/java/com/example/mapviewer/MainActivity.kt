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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.PopupProperties


// Stores name and some of database useful functions
class DatabaseHelper(private val context: Context){
    private val dbName = "hr.mbtiles"
    private val dbPath = context.getDatabasePath(dbName).path


    fun copyDatabaseIfNeeded() {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()

            context.assets.open(dbName).use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    fun getDatabasePath(): String {
        return dbPath
    }

}


class QueryDatabase(private val context: Context) {

    fun runQuery(toQuery: String): List<String> {
        val dbHelper = DatabaseHelper(context)
        dbHelper.copyDatabaseIfNeeded()

        val db = SQLiteDatabase.openDatabase(dbHelper.getDatabasePath(), null, SQLiteDatabase.OPEN_READONLY)

        val cursor = db.rawQuery("SELECT $toQuery FROM tiles", null)
        val results = mutableListOf<String>()

        val columnCount = cursor.columnCount

        while (cursor.moveToNext()) {
            val row = (0 until columnCount).joinToString { i ->
                cursor.getString(i) ?: ""
            }

            results.add(row)
        }


        cursor.close()
        db.close()

        return results
    }
}












class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = QueryDatabase(this)
        val queryDB = db.runQuery("zoom_level, tile_row")



        setContent {
            MapViewerTheme {


                //Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    //ListQuery(queryDB = queryDB, modifier = Modifier.padding(innerPadding))



                //}

                DraggableMap(this)
                
            }
        }
    }
}


@Composable
fun ListQuery(queryDB: List<String>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(queryDB) {tile ->
            Text(text = tile)
        }
    }
}







class Tile(val column: Int, val row: Int, val zoom: Int, private val context: Context) {

    var image: Bitmap? by mutableStateOf(null)
    init {

        Log.d("Tile creation", "column = $column, row = $row, zoom_level = $zoom")
        //loadImage(column, row, zoom)
    }

    fun loadImageAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = loadImage()
            withContext(Dispatchers.Main) {
                image = bitmap
            }
        }
    }

    fun loadImage(): Bitmap? {
        Log.d("tile loading", "New Tile loaded")
        val dbHelper = DatabaseHelper(context)
        dbHelper.copyDatabaseIfNeeded()

        val db = SQLiteDatabase.openDatabase(dbHelper.getDatabasePath(), null, SQLiteDatabase.OPEN_READONLY)
        val queryString: String = """
            WITH maxmin AS (
            	SELECT
            		MAX(tile_row) as max_row,
            		MIN(tile_column) as min_column
            	FROM tiles
            	WHERE zoom_level = $zoom)
            		
            SELECT 
            	t.tile_data
            FROM tiles t, maxmin m
            WHERE t.zoom_level = $zoom
            AND t.tile_column - m.min_column = $column
            AND m.max_row - t.tile_row = $row
            """

        val cursor = db.rawQuery(queryString, null)
        var bitmap: Bitmap? = null

        if (cursor.moveToFirst()) {
            val blob = cursor.getBlob(0)
            bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.size)


        }
        cursor.close()
        db.close()

        return bitmap
    }


}

class TileMap(val zoom: Int, val map_x_init: Double = 0.0, val map_y_init: Double = 0.0, private val context: Context) {
    var map_x: Double by mutableStateOf(map_x_init)
    var map_y: Double by mutableStateOf(map_y_init)
    val grid_offset = 256
    val screenWidth: Int get() = context.resources.displayMetrics.widthPixels
    val screenHeight: Int get() = context.resources.displayMetrics.heightPixels

    val grid_column: Int = screenWidth / 2 / grid_offset
    val grid_row: Int = screenHeight / 2 / grid_offset
    val last_grid_column: Int = screenWidth / 2 / grid_offset
    val last_grid_row: Int = screenHeight / 2 / grid_offset

    val render_distance_x: Int = 3
    val render_area_x: Int = render_distance_x * 2 + 1
    val render_distance_y: Int = 6
    val render_area_y: Int = render_distance_y * 2 + 1
    val first_time = true

    val tiles = mutableMapOf<Pair<Int, Int>, Tile>()

    var max_row: Int = 0
    var min_column: Int = 0

    init {
        val dbHelper = DatabaseHelper(context)
        dbHelper.copyDatabaseIfNeeded()

        val db = SQLiteDatabase.openDatabase(dbHelper.getDatabasePath(), null, SQLiteDatabase.OPEN_READONLY)
        val queryString: String = "SELECT MAX(tile_row), MIN(tile_column) FROM tiles WHERE zoom_level = $zoom"
        val cursor = db.rawQuery(queryString, null)

        if (cursor.moveToFirst()){

            max_row = cursor.getInt(0)
            min_column = cursor.getInt(1)
        }

        cursor.close()
        db.close()
    }

    fun mapLoading(x: Int = screenWidth / 2, y: Int = screenHeight / 2) {
        Log.d("First LOAD", "FIRST LOAD")
        val current_tile_column: Int = (x - map_x.toInt()) / 256
        val current_tile_row: Int = (y - map_y.toInt()) / 256
        val tilesToKeep = mutableSetOf<Pair<Int, Int>>()

        for (fy in 0 until render_area_y) {
            val zy: Int = (current_tile_row + fy) - render_distance_y
            for (fx in 0 until render_area_x) {
                val zx: Int = (current_tile_column + fx) - render_distance_x
                tiles.getOrPut(Pair(zx, zy)) { Tile(zx, zy, zoom, context).also { it.loadImageAsync()}}
                tilesToKeep.add(Pair(zx, zy))
            }
        }

        tiles.entries.removeIf { (key, _) ->
            key !in tilesToKeep
        }
    }

    fun xyToLatLong(x: Double = screenWidth / 2.0, y: Double = screenHeight / 2.0): Pair<Double, Double> {
        //Log.d("a","a")
        Log.d("cords", "$x, $y")
        val scaled_mouse_x = (x - map_x) % 256
        val scaled_mouse_y = (y - map_y) % 256
        //Log.d("", "$scaled_mouse_x, $scaled_mouse_y")

        var current_tile_column: Int = ((x - map_x) / 256).toInt()
        current_tile_column = min_column + current_tile_column
        var current_tile_row: Int = ((y - map_y) / 256).toInt()
        current_tile_row = max_row - current_tile_row

        //Log.d("", "$current_tile_column, $current_tile_row")


        val n: Double = 2.0.pow(zoom)
        //Log.d("", "n: $n")

        val lat_part: Double = (current_tile_row + 1 - scaled_mouse_y / 256) / n
        val long_part: Double = (current_tile_column + scaled_mouse_x / 256) / n
        //Log.d("", "$lat_part, $long_part")
        val lat = Math.toDegrees(atan(sinh(PI * (1 - 2 * lat_part)))) * -1
        val long = long_part * 360 - 180
        //Log.d("xyToLatLong", "Lat: $lat, Long: $long")
        Log.d("go", "go $lat, $long")

        latLongToxy(lat, long)

        //Log.d("a","a")
        return Pair(lat, long)
    }

    fun latLongToxy(lat: Double, long: Double): Pair<Double, Double> {
        val n: Double = 2.0.pow(zoom)

        val long_part = (180 + long) / 360
        val lat_part = (PI + asinh(tan(Math.toRadians(lat)))) / (-2 * PI) * -1


        val tile_column = floor(long_part * n)
        val tile_row = ceil(n * lat_part - 1)

        var scaled_mouse_x: Double = round((long_part * n - tile_column) * 256)
        var scaled_mouse_y: Double = round((lat_part * n - tile_row - 1) * -256)



        scaled_mouse_x += (tile_column - min_column) * 256
        scaled_mouse_y += (max_row - tile_row) * 256
        //Log.d("latLongToxy", "scaled_mouse_x: ${scaled_mouse_x + map_x}, scaled_mouse_y: ${scaled_mouse_y + map_y}")
        //Log.d("latLongToxy", "scaled_mouse_x: $scaled_mouse_x, scaled_mouse_y: $scaled_mouse_y")
        return Pair(scaled_mouse_x, scaled_mouse_y)
    }


}

class Pins() {
    class Pin(val lat: Double, val long: Double, val min_column: Int, val max_row: Int, val zoom_level: Int) {
        var x: Double = 0.0
        var y: Double = 0.0

        init {
            latLongToXY()
        }

        fun latLongToXY(zoom_level_b: Int = -1, min_column_b: Int = -1, max_row_b: Int = -1) {
            var zoom = zoom_level_b
            if (zoom == -1) { zoom = zoom_level}
            var min_col = min_column_b
            if (min_col == -1) { min_col = min_column }
            var max_rw = max_row_b
            if (max_rw == -1) { max_rw = max_row }

            val n = 2.0.pow(zoom)
            var xa = (180 + long) / 360
            var ya =  (PI + asinh(tan(Math.toRadians(lat)))) / (-2 * PI) * -1

            val tile_column = floor(xa * n)
            val tile_row = ceil(n * ya - 1)

            xa = (xa * n - tile_column) * 256
            ya = (ya * n - tile_row - 1) * -256


            x = xa + (tile_column - min_col) * 256
            y = ya + (max_rw - tile_row) * 256

        }


    }

    val listOfPins = mutableStateListOf<Pin>()
    /*init {
        listOfPins.add(Offset(50f, 50f))
        listOfPins.add(Offset(150f, 250f))
        listOfPins.add(Offset(450f, 150f))
    }*/

    fun addPin(lat: Double, long: Double, min_column: Int, max_row: Int, zoom_level: Int) {
        listOfPins.add(Pin(lat, long, min_column , max_row, zoom_level))
        //listOfPins.add(Offset(x.toFloat(), y.toFloat()))
    }

    fun clearPins() {
        listOfPins.clear()
    }

    fun popPin() {
        if (listOfPins.isNotEmpty()) {
            listOfPins.removeAt(listOfPins.lastIndex)
        }
    }

    fun recalculatePins(zoom_level: Int, min_column: Int, max_row: Int) {
        for (pin in listOfPins) {
            pin.latLongToXY(zoom_level, min_column, max_row)
        }
    }
    // TODO: maybe
    //fun drawPins()

    fun calculateDistance(): Double {
        var full_distance_knots: Double = 0.0

        val r = 6371
        for (i in 0 until listOfPins.size) {
            if (i > 0) {
                val root: Double = Math.sqrt(
                    (Math.sin((Math.toRadians(listOfPins[i].lat) - Math.toRadians(listOfPins[i-1].lat)) / 2).pow(2))
                            + Math.cos(Math.toRadians(listOfPins[i-1].lat)) * Math.cos(Math.toRadians(listOfPins[i].lat)) *
                            (Math.sin((Math.toRadians(listOfPins[i].long) - Math.toRadians(listOfPins[i-1].long)) / 2).pow(2)))

                val distance_km: Double = 2 * r * Math.asin(root)
                val distance_knots: Double = distance_km * 0.5399570136728

                full_distance_knots += distance_knots
            }
        }

        return full_distance_knots
    }

    fun calculateTime(speed: Double): String {
        val distance = calculateDistance()
        val hours: Int = (distance / speed).toInt()
        val minutes: Int = (((distance / speed) - hours) * 60).toInt()
        val seconds: Int = (((((distance / speed) - hours) * 60) - minutes) * 60).toInt()
        val time: String = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        return time
    }

    fun fuelConsumption(time: String, consumedFuel: Double): Double {
        val split = time.split(":")

        val hours = split[0].toInt()
        val minutes = split[1].toInt()
        val seconds = split[2].toInt()
        val time = hours + minutes / 60 + seconds / 3600

        val fuel_consumption = round(consumedFuel / time * 100) / 100

        return fuel_consumption
    }

    fun avgSpeed(time: String, dist: Double = -1.0): Double {
        var distance = dist
        if (distance == -1.0) { distance = calculateDistance() }

        val split = time.split(":")

        val hours = split[0].toInt()
        val minutes = split[1].toInt()
        val seconds = split[2].toInt()
        val time = hours + minutes / 60 + seconds / 3600

        val avg_speed: Double = distance / time
        return avg_speed
    }
}



@Composable
fun DraggableMap(context: Context) {





    var tileMap by remember { mutableStateOf(TileMap(10, -2650.0, -1100.0, context)) }
    var zoom_level by remember {mutableIntStateOf(10)}
    var pins by remember {mutableStateOf(Pins())}

    val (max_zoom, min_zoom) = remember {
        val dbHelper = DatabaseHelper(context)
        dbHelper.copyDatabaseIfNeeded()

        val db = SQLiteDatabase.openDatabase(dbHelper.getDatabasePath(), null, SQLiteDatabase.OPEN_READONLY)
        val queryString: String = "SELECT MAX(zoom_level), MIN(zoom_level) FROM tiles"
        val cursor = db.rawQuery(queryString, null)

        var max = 0
        var min = 0
        if (cursor.moveToFirst()){

            max = cursor.getInt(0)
            min = cursor.getInt(1)
        }

        cursor.close()
        db.close()

        Pair(max, min)
    }


    tileMap.mapLoading()
    var clicked by remember {mutableStateOf(false)}
    var scaleF by remember { mutableFloatStateOf(1f) }
    val scope = rememberCoroutineScope()
    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    Log.d("Tap gesture", "Tap gesture: ${offset.x}, ${offset.y}")
                    //val (lat, long) = tileMap.xyToLatLong((offset.x.toDouble() - tileMap.map_x), (offset.y.toDouble() - tileMap.map_y))
                    Log.d("go", "Scale: $scaleF")
                    //val (lat, long) = tileMap.xyToLatLong((offset.x.toDouble()), (offset.y.toDouble()))

                    val (lat, long) = tileMap.xyToLatLong(
                        (offset.x.toDouble() - tileMap.screenWidth / 2) / scaleF + tileMap.screenWidth / 2,
                        (offset.y.toDouble() - tileMap.screenHeight / 2) / scaleF + tileMap.screenHeight / 2
                    )


                    //pins.addPin((offset.x + tileMap.map_x).toFloat(), (offset.y + tileMap.map_y).toFloat())
                    //pins.addPin((offset.x).toFloat(), (offset.y).toFloat())
                    //val (lat, long) = tileMap.xyToLatLong(offset.x.toDouble(), offset.y.toDouble())
                    pins.addPin(lat, long, tileMap.min_column, tileMap.max_row, tileMap.zoom)

                    //Log.d("size of ", "Size of pins: ${pins.listOfPins.size}")
                    clicked = true
                    scope.launch {
                        delay(500)
                        clicked = false
                    }
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    Log.d("loaded tiles", "${tileMap.tiles.size}")
                    //tileMap.map_x += (pan.x).toInt()
                    //tileMap.map_y += (pan.y).toInt()

                    tileMap.map_x += pan.x / scaleF
                    tileMap.map_y += pan.y / scaleF
                    scaleF *= zoom

                    if (scaleF > 1.5f) { // zoom in
                        Log.d("scale", "ScaleF je veci of 1.5f $scaleF")
                        if (zoom_level < max_zoom) {
                            val (lat, long) = tileMap.xyToLatLong()
                            zoom_level += 1
                            tileMap = TileMap(zoom_level, context = context)

                            val (xa, ya) = tileMap.latLongToxy(lat, long)
                            tileMap.map_x = tileMap.screenWidth / 2 - xa
                            tileMap.map_y = tileMap.screenHeight / 2 - ya

                            pins.recalculatePins(tileMap.zoom, tileMap.min_column, tileMap.max_row)

                            scaleF = 0.75f
                        }




                    }
                    else if (scaleF < 0.75f) { // zoom out
                        Log.d("scale", "ScaleF je veci of 1.5f $scaleF")
                        if (zoom_level > min_zoom) {
                            val (lat, long) = tileMap.xyToLatLong()
                            zoom_level -= 1
                            tileMap = TileMap(zoom_level, context = context)

                            val (xa, ya) = tileMap.latLongToxy(lat, long)
                            tileMap.map_x = tileMap.screenWidth / 2 - xa
                            tileMap.map_y = tileMap.screenHeight / 2 - ya

                            pins.recalculatePins(tileMap.zoom, tileMap.min_column, tileMap.max_row)

                            scaleF = 1.50f
                        }

                    }
                    Log.d("pinch zoom", "Scale: ${scaleF}")
                }
                /*detectDragGestures { change, dragAmount ->
                    change.consume()
                    Log.d("loaded tiles", "${tileMap.tiles.size}")
                    tileMap.map_x += (dragAmount.x).toInt()
                    tileMap.map_y += (dragAmount.y).toInt()
                }*/


            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .graphicsLayer(scaleX = scaleF, scaleY = scaleF)
        ) {
            for ((_, tile) in tileMap.tiles) {
                tile.image?.let { bitmap ->

                    Image(
                        painter = BitmapPainter(bitmap.asImageBitmap()),
                        contentDescription = null,
                        modifier = Modifier.offset {
                            IntOffset(
                                x = (tile.column * tileMap.grid_offset + tileMap.map_x).toInt(),
                                y = (tile.row * tileMap.grid_offset + tileMap.map_y).toInt()
                            )
                        }

                    )
                }

            }

            Canvas(modifier = Modifier.fillMaxSize())
            {

                if (pins.listOfPins.size > 0) {
                    //for (pin in pins.listOfPins) {
                    for (i in 0 until pins.listOfPins.size) {
                        //Log.d("pin.x , pin.y", "${pin.x}, ${pin.y}")
                        //Log.d("drawCircle", "drawing circle")
                        //val x: Float = (pin.x + tileMap.map_x).toFloat()
                        //val y: Float = (pin.y + tileMap.map_y).toFloat()
                        //Log.d("xy", "$x, $y")
                        //drawCircle(color = Color.Red, radius = 20f, center = Offset(pin.x,pin.y))
                        //drawCircle(color = Color.Red, radius = 20f / scaleF, center = Offset((pin.x + tileMap.map_x).toFloat(), (pin.y + tileMap.map_y).toFloat()))
                        drawCircle(color = Color.Red, radius = 20f / scaleF, center = Offset((pins.listOfPins[i].x + tileMap.map_x).toFloat(), (pins.listOfPins[i].y + tileMap.map_y).toFloat()))

                        if (i > 0) {
                            drawLine(
                                color = Color.Magenta,
                                start = Offset((pins.listOfPins[i-1].x + tileMap.map_x).toFloat(), (pins.listOfPins[i-1].y + tileMap.map_y).toFloat()),
                                end = Offset((pins.listOfPins[i].x + tileMap.map_x).toFloat(), (pins.listOfPins[i].y + tileMap.map_y).toFloat()),
                                strokeWidth = 2f)
                        }
                    }
                }
            }
        }

        // Bottom box of the screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(Color.Red.copy(alpha = 0.7f))

        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 50.dp)

                ,
                //horizontalArrangement = Arrangement.spacedBy(15.dp)
                horizontalArrangement = Arrangement.End

            ) {




                Button(
                    onClick = {
                        pins.popPin()
                    },
                    modifier = Modifier
                        .height(80.dp)
                        .width(110.dp)
                        .padding(10.dp)
                    //.offset(x = -150.dp)
                    //.offset(y = (-30).dp)
                    //.align(Alignment.BottomEnd)
                    ,
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Undo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        maxLines = 1,
                        softWrap = false
                    )
                }


                Button(
                    onClick = {
                        pins.clearPins()
                    },
                    modifier = Modifier
                        .height(80.dp)
                        .width(110.dp)
                        .padding(10.dp)
                        //.offset(x = -20.dp)
                        //.offset(y = (-30).dp)
                        //.align(Alignment.BottomEnd)
                        ,shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Clear",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        maxLines = 1,
                        softWrap = false
                    )
                }


            }

        }




        ////////////////////////////////////////////////////////////////////////////////
        var isFocused by remember { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current

        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        // This makes clicks on empty space dismiss the keyboard
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                    }
                    .background(Color.Green.copy(alpha = 0.5f))

            )
        }

        // Top box of the screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
                .background(Color.Blue.copy(alpha = 0.7f))
        ) {
            Column (
                modifier = Modifier.padding(start = 20.dp, top = 50.dp)
            ){
                Text("Distance: ${round(pins.calculateDistance() * 100) / 100} knots",
                    //modifier = Modifier.offset(x = 20.dp, y = 50.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold)
                Text("AAAAA")

                var text = remember { mutableStateOf("") }

                Text("Hours")


                //val keyboardController = LocalSoftwareKeyboardController.current

                OutlinedTextField(
                    value = text.value,
                    onValueChange = { if (it.length <= 2 ) text.value = it },
                    //label = { Text("00") },
                    placeholder = { Text("00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp)
                        //.padding(10.dp)

                )
                Log.d("s", "$text")


                Text("Minutes")


                //val keyboardController = LocalSoftwareKeyboardController.current

                OutlinedTextField(
                    value = text.value,
                    onValueChange = { if (it.length <= 2 ) text.value = it },
                    //label = { Text("00") },
                    placeholder = { Text("00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp)
                    //.padding(10.dp)

                )

                //var isFocused by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .border(width = if(isFocused) 3.dp else 1.dp, Color.Gray, shape = RoundedCornerShape(5.dp))
                        .width(100.dp)
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                )
                {
                    if (text.value.isEmpty()) {
                        Text(
                            text = "00",
                            fontSize = 18.sp, color = Color.LightGray,
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxSize()
                                .padding(bottom = 2.dp)
                                //.align(Alignment.Center)
                            )

                    }

                    BasicTextField(
                        value = text.value,
                        onValueChange = { if (it.length <= 2) text.value = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(fontSize = 18.sp, color = Color.White),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                            .onFocusChanged{ focusState ->
                                isFocused = focusState.isFocused
                            }
                            //.align(Alignment.Center)
                    )



                }





            }
        }




    }



}





























/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MapViewerTheme {
        Greeting("Android")
    }
}
 */