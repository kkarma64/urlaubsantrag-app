package de.bauhaupt.urlaubsschein.ui.screens.more

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.data.security.SecurePreferences
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import java.io.File
import java.io.FileOutputStream

@Composable
fun UnterschriftScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val securePrefs = remember { SecurePreferences(context) }

    var signaturePath by remember { mutableStateOf(securePrefs.signaturePath) }
    var isDrawingMode by remember { mutableStateOf(false) }
    var aktuelleGruppe by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var zeichnungPunkte by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val sigFile = File(context.filesDir, "user_signature.png")
                    FileOutputStream(sigFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    signaturePath = sigFile.absolutePath
                    securePrefs.signaturePath = signaturePath
                    Toast.makeText(context, "Unterschrift gespeichert", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    GridBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Unterschrift",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onBack) {
                    Text("Zurück", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("UNTERSCHRIFT VERWALTEN", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    if (signaturePath.isNotBlank()) {
                        val file = File(signaturePath)
                        if (file.exists()) {
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            if (bitmap != null) {
                                var normY by remember { mutableStateOf(securePrefs.signatureNormY) }
                                var scale by remember { mutableStateOf(securePrefs.signatureScale) }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Unterschrift-Ausrichtung & Skalierung:", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .background(Color.White, RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val baselineY = size.height * normY
                                            drawLine(
                                                color = Color.Gray,
                                                start = Offset(0f, baselineY),
                                                end = Offset(size.width, baselineY),
                                                strokeWidth = 2f,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                            )
                                        }
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Unterschrift Vorschau",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(scaleX = scale, scaleY = scale)
                                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Höhe:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
                                        Slider(
                                            value = normY,
                                            onValueChange = {
                                                normY = it
                                                securePrefs.signatureNormY = it
                                            },
                                            valueRange = 0.2f..1.2f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary,
                                                activeTrackColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Größe:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
                                        Slider(
                                            value = scale,
                                            onValueChange = {
                                                scale = it
                                                securePrefs.signatureScale = it
                                            },
                                            valueRange = 0.5f..2.0f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary,
                                                activeTrackColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { isDrawingMode = !isDrawingMode },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDrawingMode) Color.DarkGray else MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isDrawingMode) "Zeichnen schließen" else "Direkt zeichnen",
                                color = if (isDrawingMode) Color.White else MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Bilddatei wählen", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isDrawingMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Unterschreibe auf der Linie:", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .onSizeChanged { canvasSize = it }
                                .pointerInput(canvasSize) {
                                    if (canvasSize.width == 0) return@pointerInput
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val norm = Offset(offset.x / canvasSize.width, offset.y / canvasSize.height)
                                            aktuelleGruppe = listOf(norm)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val norm = Offset(change.position.x / canvasSize.width, change.position.y / canvasSize.height)
                                            aktuelleGruppe = aktuelleGruppe + norm
                                        },
                                        onDragEnd = {
                                            if (aktuelleGruppe.isNotEmpty()) {
                                                zeichnungPunkte = zeichnungPunkte + listOf(aktuelleGruppe)
                                                aktuelleGruppe = emptyList()
                                            }
                                        }
                                    )
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val lineColor = Color.Gray.copy(alpha = 0.5f)
                                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                val grundLinieY = size.height * 0.7f

                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, grundLinieY),
                                    end = Offset(size.width, grundLinieY),
                                    strokeWidth = 2f,
                                    pathEffect = dashEffect
                                )

                                zeichnungPunkte.forEach { gruppeNorm ->
                                    val gruppe = gruppeNorm.map { Offset(it.x * size.width, it.y * size.height) }
                                    if (gruppe.size > 1) {
                                        for (i in 0 until gruppe.size - 1) {
                                            drawLine(Color.Black, gruppe[i], gruppe[i + 1], strokeWidth = 6f, cap = StrokeCap.Round)
                                        }
                                    }
                                }

                                val gruppeAktuell = aktuelleGruppe.map { Offset(it.x * size.width, it.y * size.height) }
                                if (gruppeAktuell.size > 1) {
                                    for (i in 0 until gruppeAktuell.size - 1) {
                                        drawLine(Color.Black, gruppeAktuell[i], gruppeAktuell[i + 1], strokeWidth = 6f, cap = StrokeCap.Round)
                                    }
                                }
                            }
                            if (zeichnungPunkte.isEmpty() && aktuelleGruppe.isEmpty()) {
                                Text("Hier unterschreiben", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { zeichnungPunkte = emptyList(); aktuelleGruppe = emptyList() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Löschen", color = MaterialTheme.colorScheme.onSurface)
                            }
                            Button(
                                onClick = {
                                    if (zeichnungPunkte.isNotEmpty() && canvasSize.width > 0 && canvasSize.height > 0) {
                                        val targetWidth = 1200
                                        val targetHeight = (targetWidth.toFloat() * canvasSize.height / canvasSize.width).toInt()
                                        val scaleX = targetWidth.toFloat() / canvasSize.width

                                        val bmp = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                                        val canvas = android.graphics.Canvas(bmp)
                                        val paint = android.graphics.Paint().apply {
                                            color = android.graphics.Color.BLACK
                                            strokeWidth = 12f
                                            style = android.graphics.Paint.Style.STROKE
                                            strokeCap = android.graphics.Paint.Cap.ROUND
                                            isAntiAlias = true
                                        }
                                        zeichnungPunkte.forEach { gruppe ->
                                            if (gruppe.size > 1) {
                                                val path = android.graphics.Path()
                                                path.moveTo(gruppe[0].x * canvasSize.width * scaleX, gruppe[0].y * canvasSize.height * scaleX)
                                                for (i in 1 until gruppe.size) {
                                                    path.lineTo(gruppe[i].x * canvasSize.width * scaleX, gruppe[i].y * canvasSize.height * scaleX)
                                                }
                                                canvas.drawPath(path, paint)
                                            }
                                        }
                                        val sigFile = File(context.filesDir, "drawn_signature.png")
                                        FileOutputStream(sigFile).use { out ->
                                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                                        }
                                        signaturePath = sigFile.absolutePath
                                        securePrefs.signaturePath = signaturePath
                                        isDrawingMode = false
                                        Toast.makeText(context, "Unterschrift gespeichert", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Übernehmen", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Button "Unterschrift (Einstellungen) speichern" am Ende des Screens
            Button(
                onClick = {
                    securePrefs.signaturePath = signaturePath
                    Toast.makeText(context, "Unterschrift gespeichert", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Unterschrift (Einstellungen) speichern", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
