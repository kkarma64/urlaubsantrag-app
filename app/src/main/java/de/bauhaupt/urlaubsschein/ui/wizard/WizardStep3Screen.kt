package de.bauhaupt.urlaubsschein.ui.wizard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas as ComposeCanvas
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.data.security.SecurePreferences
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import de.bauhaupt.urlaubsschein.ui.theme.StatusGreen
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

@Composable
fun WizardStep3Screen(
    viewModel: WizardViewModel,
    onSendClick: (File?) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val scope = rememberCoroutineScope()

    val securePrefs = remember { SecurePreferences(context) }
    val profileSigPath = securePrefs.signaturePath
    val hasProfileSig = profileSigPath.isNotBlank() && File(profileSigPath).exists()

    var aktuelleGruppe by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showOverlapDialog by remember { mutableStateOf(false) }
    var pendingSignatureFile by remember { mutableStateOf<File?>(null) }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Erfolg", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Der Antrag wurde erfolgreich generiert und an den E-Mail-Client übergeben.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onBackClick()
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showOverlapDialog) {
        AlertDialog(
            onDismissRequest = { showOverlapDialog = false },
            title = { Text("Überschneidung erkannt", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Der gewählte Urlaubszeitraum überschneidet sich mit einem bereits existierenden Antrag. Möchtest du trotzdem fortfahren?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                Button(
                    onClick = {
                        showOverlapDialog = false
                        viewModel.submitAntrag(context, pendingSignatureFile) {
                            showSuccessDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Trotzdem fortfahren", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlapDialog = false }) {
                    Text("Abbrechen", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    GridBackground {
        if (isLandscape) {
            // --- QUERFORMAT ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Neuer Antrag (3/3)", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (hasProfileSig) {
                    Button(
                        onClick = {
                            viewModel.setUnterschriftGezeichnet()
                            val file = File(profileSigPath)
                            scope.launch {
                                if (viewModel.hatUeberschneidung()) {
                                    pendingSignatureFile = file
                                    showOverlapDialog = true
                                } else {
                                    viewModel.submitAntrag(context, file) {
                                        showSuccessDialog = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("✓ Profil-Unterschrift verwenden", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Oder direkt auf dem Bildschirm zeichnen:", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
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
                                        viewModel.updateZeichnung(state.zeichnungPunkte + listOf(aktuelleGruppe))
                                        aktuelleGruppe = emptyList()
                                        viewModel.setUnterschriftGezeichnet()
                                    }
                                }
                            )
                        }
                ) {
                    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                        val lineColor = Color.Gray.copy(alpha = 0.4f)
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        val grundLinieY = size.height * (69f / 96f)

                        drawLine(
                            color = lineColor,
                            start = Offset(0f, grundLinieY),
                            end = Offset(size.width, grundLinieY),
                            strokeWidth = 2f,
                            pathEffect = dashEffect
                        )

                        state.zeichnungPunkte.forEach { gruppeNorm ->
                            val gruppe = gruppeNorm.map { Offset(it.x * size.width, it.y * size.height) }
                            if (gruppe.size > 1) {
                                for (i in 0 until gruppe.size - 1) {
                                    drawLine(Color.Black, gruppe[i], gruppe[i + 1], strokeWidth = 8f, cap = StrokeCap.Round)
                                }
                            }
                        }

                        val gruppeAktuell = aktuelleGruppe.map { Offset(it.x * size.width, it.y * size.height) }
                        if (gruppeAktuell.size > 1) {
                            for (i in 0 until gruppeAktuell.size - 1) {
                                drawLine(Color.Black, gruppeAktuell[i], gruppeAktuell[i + 1], strokeWidth = 8f, cap = StrokeCap.Round)
                            }
                        }
                    }
                    if (state.zeichnungPunkte.isEmpty() && aktuelleGruppe.isEmpty()) {
                        Text("Hier mit dem Finger unterschreiben", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.updateZeichnung(emptyList())
                            aktuelleGruppe = emptyList()
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Löschen", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (canvasSize.height > 0 && state.zeichnungPunkte.isNotEmpty()) {
                                var minY = Float.MAX_VALUE
                                var maxY = Float.MIN_VALUE
                                state.zeichnungPunkte.forEach { gruppeNorm ->
                                    gruppeNorm.forEach { point ->
                                        val pixelY = point.y * canvasSize.height
                                        if (pixelY < minY) minY = pixelY
                                        if (pixelY > maxY) maxY = pixelY
                                    }
                                }
                                val schreibLinieYCanvas = canvasSize.height * (69f / 96f)
                                val relativeSchreibLinieY = (schreibLinieYCanvas - minY) / (maxY - minY)
                                viewModel.updateSchreibLinieY(relativeSchreibLinieY)
                            }
                            processSignatureAndSubmit(context, state.zeichnungPunkte, canvasSize, scope, viewModel, {
                                pendingSignatureFile = it
                                showOverlapDialog = true
                            }, {
                                showSuccessDialog = true
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.weight(2f).height(50.dp),
                        enabled = state.zeichnungPunkte.isNotEmpty()
                    ) {
                        Text("Fertig", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBackClick) {
                    Text("Zurück", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                }
            }
        } else {
            // --- HOCHFORMAT ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Neuer Antrag (3/3)", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { 1.0f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline,
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("UNTERSCHRIFT ERFASSEN", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (hasProfileSig) {
                    Button(
                        onClick = {
                            viewModel.setUnterschriftGezeichnet()
                            val file = File(profileSigPath)
                            scope.launch {
                                if (viewModel.hatUeberschneidung()) {
                                    pendingSignatureFile = file
                                    showOverlapDialog = true
                                } else {
                                    viewModel.submitAntrag(context, file) {
                                        showSuccessDialog = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("✓ Profil-Unterschrift verwenden", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Oder direkt auf dem Bildschirm zeichnen:", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
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
                                        viewModel.updateZeichnung(state.zeichnungPunkte + listOf(aktuelleGruppe))
                                        aktuelleGruppe = emptyList()
                                        viewModel.setUnterschriftGezeichnet()
                                    }
                                }
                            )
                        }
                ) {
                    ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                        val lineColor = Color.Gray.copy(alpha = 0.4f)
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        val grundLinieY = size.height * (69f / 96f)

                        drawLine(
                            color = lineColor,
                            start = Offset(0f, grundLinieY),
                            end = Offset(size.width, grundLinieY),
                            strokeWidth = 2f,
                            pathEffect = dashEffect
                        )

                        state.zeichnungPunkte.forEach { gruppeNorm ->
                            val gruppe = gruppeNorm.map { Offset(it.x * size.width, it.y * size.height) }
                            if (gruppe.size > 1) {
                                for (i in 0 until gruppe.size - 1) {
                                    drawLine(Color.Black, gruppe[i], gruppe[i + 1], strokeWidth = 8f, cap = StrokeCap.Round)
                                }
                            }
                        }

                        val gruppeAktuell = aktuelleGruppe.map { Offset(it.x * size.width, it.y * size.height) }
                        if (gruppeAktuell.size > 1) {
                            for (i in 0 until gruppeAktuell.size - 1) {
                                drawLine(Color.Black, gruppeAktuell[i], gruppeAktuell[i + 1], strokeWidth = 8f, cap = StrokeCap.Round)
                            }
                        }
                    }
                    if (state.zeichnungPunkte.isEmpty() && aktuelleGruppe.isEmpty()) {
                        Text("Hier mit dem Finger unterschreiben", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Zusammenfassung", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${state.urlaubsArt} • ${state.berechneteTage} Tage", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        Text("Baustelle: ${state.baustelle}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.updateZeichnung(emptyList())
                            aktuelleGruppe = emptyList()
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Löschen", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (canvasSize.height > 0 && state.zeichnungPunkte.isNotEmpty()) {
                                var minY = Float.MAX_VALUE
                                var maxY = Float.MIN_VALUE
                                state.zeichnungPunkte.forEach { gruppeNorm ->
                                    gruppeNorm.forEach { point ->
                                        val pixelY = point.y * canvasSize.height
                                        if (pixelY < minY) minY = pixelY
                                        if (pixelY > maxY) maxY = pixelY
                                    }
                                }
                                val schreibLinieYCanvas = canvasSize.height * (69f / 96f)
                                val relativeSchreibLinieY = (schreibLinieYCanvas - minY) / (maxY - minY)
                                viewModel.updateSchreibLinieY(relativeSchreibLinieY)
                            }
                            processSignatureAndSubmit(context, state.zeichnungPunkte, canvasSize, scope, viewModel, {
                                pendingSignatureFile = it
                                showOverlapDialog = true
                            }, {
                                showSuccessDialog = true
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.weight(2f).height(50.dp),
                        enabled = state.zeichnungPunkte.isNotEmpty()
                    ) {
                        Text("Unterschrift fertig", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun processSignatureAndSubmit(
    context: android.content.Context,
    punkteGruppenNorm: List<List<Offset>>,
    canvasSize: IntSize,
    scope: kotlinx.coroutines.CoroutineScope,
    viewModel: WizardViewModel,
    onOverlap: (File?) -> Unit,
    onSuccess: () -> Unit
) {
    val file = if (punkteGruppenNorm.isNotEmpty() && canvasSize.width > 0) {
        val punktePixel = punkteGruppenNorm.map { gruppe ->
            gruppe.map { Offset(it.x * canvasSize.width, it.y * canvasSize.height) }
        }

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        punktePixel.forEach { gruppe ->
            gruppe.forEach { point ->
                if (point.x < minX) minX = point.x
                if (point.y < minY) minY = point.y
                if (point.x > maxX) maxX = point.x
                if (point.y > maxY) maxY = point.y
            }
        }

        val padding = 10f
        minX = max(0f, minX - padding)
        minY = max(0f, minY - padding)
        maxX = min(canvasSize.width.toFloat(), maxX + padding)
        maxY = min(canvasSize.height.toFloat(), maxY + padding)

        val width = (maxX - minX).toInt()
        val height = (maxY - minY).toInt()

        if (width <= 0 || height <= 0) {
            return
        }

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.translate(-minX, -minY)

        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        punktePixel.forEach { gruppe ->
            if (gruppe.size > 1) {
                val path = Path()
                path.moveTo(gruppe[0].x, gruppe[0].y)
                for (i in 1 until gruppe.size) {
                    path.lineTo(gruppe[i].x, gruppe[i].y)
                }
                canvas.drawPath(path, paint)
            }
        }

        val tempFile = File(context.cacheDir, "temp_signature.png")
        FileOutputStream(tempFile).use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        tempFile
    } else null

    scope.launch {
        if (viewModel.hatUeberschneidung()) {
            onOverlap(file)
        } else {
            viewModel.submitAntrag(context, file) {
                onSuccess()
            }
        }
    }
}
