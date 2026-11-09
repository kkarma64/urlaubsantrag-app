package de.bauhaupt.urlaubsschein.ui.wizard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.data.repository.AppPreferencesRepository
import de.bauhaupt.urlaubsschein.data.security.SecurePreferences
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import de.bauhaupt.urlaubsschein.ui.theme.StatusGreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
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
    val appPrefs = remember { AppPreferencesRepository(context) }
    val profileSigPath = securePrefs.signaturePath
    val hasProfileSig = profileSigPath.isNotBlank() && File(profileSigPath).exists()

    var aktuelleGruppe by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showOverlapDialog by remember { mutableStateOf(false) }
    var showReviewScreen by remember { mutableStateOf(false) }
    var capturedSignatureFile by remember { mutableStateOf<File?>(null) }

    var lohnbueroEmail by remember { mutableStateOf("") }
    var polierEmail by remember { mutableStateOf(securePrefs.standardPolierEmail) }

    LaunchedEffect(Unit) {
        lohnbueroEmail = appPrefs.empfaengerEmail.first() ?: "lohnbuero@nameunternehmen.de"
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN)

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
                        viewModel.submitAntragWithRecipients(context, capturedSignatureFile, lohnbueroEmail, polierEmail) {
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
        if (showReviewScreen) {
            // --- STAGE 2: LETZTE KONTROLLE VOR DEM VERSAND ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Letzte Kontrolle vor dem Versand", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Bitte kontrolliere die Antragsdetails und passe ggf. die E-Mail-Adressen an.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 13.sp)

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ANTRAGSDETAILS", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Art: ${state.urlaubsArt}", color = MaterialTheme.colorScheme.onSurface)
                        Text("Zeitraum: ${state.vonDatum.format(dateFormatter)} - ${state.bisDatum.format(dateFormatter)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text("Dauer: ${state.berechneteTage} Arbeitstage", color = MaterialTheme.colorScheme.onSurface)
                        Text("Baustelle: ${state.baustelle}", color = MaterialTheme.colorScheme.onSurface)
                        Text("Kostenstelle: ${state.kostenstelle}", color = MaterialTheme.colorScheme.onSurface)
                        Text("Polier: ${state.polier}", color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("E-MAIL EMPFÄNGER", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = lohnbueroEmail,
                            onValueChange = { lohnbueroEmail = it },
                            label = { Text("E-Mail Lohnbüro (An)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = polierEmail,
                            onValueChange = { polierEmail = it },
                            label = { Text("E-Mail Polier (CC)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showReviewScreen = false },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(30.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Zurück", color = MaterialTheme.colorScheme.onBackground)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                if (viewModel.hatUeberschneidung()) {
                                    showOverlapDialog = true
                                } else {
                                    viewModel.submitAntragWithRecipients(context, capturedSignatureFile, lohnbueroEmail, polierEmail) {
                                        showSuccessDialog = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(30.dp),
                        modifier = Modifier.weight(2f).height(56.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "PDF erzeugen & per E-Mail versenden",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // --- STAGE 1: UNTERSCHRIFT ERFASSEN ---
            if (isLandscape) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Neuer Antrag (3/3)", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (hasProfileSig) {
                        Button(
                            onClick = {
                                viewModel.setUnterschriftGezeichnet()
                                capturedSignatureFile = File(profileSigPath)
                                showReviewScreen = true
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
                                capturedSignatureFile = processSignatureToFile(context, state.zeichnungPunkte, canvasSize)
                                showReviewScreen = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.weight(2f).height(50.dp),
                            enabled = state.zeichnungPunkte.isNotEmpty()
                        ) {
                            Text("Weiter zur Kontrolle", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onBackClick) {
                        Text("Zurück", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
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
                                capturedSignatureFile = File(profileSigPath)
                                showReviewScreen = true
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
                                capturedSignatureFile = processSignatureToFile(context, state.zeichnungPunkte, canvasSize)
                                showReviewScreen = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.weight(2f).height(50.dp),
                            enabled = state.zeichnungPunkte.isNotEmpty()
                        ) {
                            Text("Weiter zur Kontrolle", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun processSignatureToFile(
    context: android.content.Context,
    punkteGruppenNorm: List<List<Offset>>,
    canvasSize: IntSize
): File? {
    if (punkteGruppenNorm.isEmpty() || canvasSize.width <= 0) return null

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

    if (width <= 0 || height <= 0) return null

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
    return tempFile
}
