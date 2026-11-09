package de.bauhaupt.urlaubsschein.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.lifecycle.viewmodel.compose.viewModel
import de.bauhaupt.urlaubsschein.MainActivity
import de.bauhaupt.urlaubsschein.data.db.DatabaseProvider
import de.bauhaupt.urlaubsschein.data.repository.AppPreferencesRepository
import de.bauhaupt.urlaubsschein.data.security.SecurePreferences
import de.bauhaupt.urlaubsschein.logic.BackupManager
import de.bauhaupt.urlaubsschein.logic.Bundesland
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import de.bauhaupt.urlaubsschein.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@Composable
fun ProfileSettingsScreen(
    onHelpClick: () -> Unit,
    onDrawingModeChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val securePrefs = remember { SecurePreferences(context) }
    val appPrefs = remember { AppPreferencesRepository(context) }
    val db = remember { DatabaseProvider.getDatabase(context) }
    val themeViewModel: ThemeViewModel = viewModel()
    val scope = rememberCoroutineScope()

    var vorname by remember { mutableStateOf(securePrefs.vorname) }
    var name by remember { mutableStateOf(securePrefs.name) }
    var pnr by remember { mutableStateOf(securePrefs.personalnummer) }
    var kostenstelle by remember { mutableStateOf(securePrefs.kostenstelle) }
    var standardPolier by remember { mutableStateOf(securePrefs.standardPolier) }
    var standardBundesland by remember { mutableStateOf(Bundesland.fromKuerzel(securePrefs.standardBundesland)) }
    var signaturePath by remember { mutableStateOf(securePrefs.signaturePath) }

    var empfaengerEmail by remember { mutableStateOf("") }
    var betreffFormat by remember { mutableStateOf("Urlaubsschein {Name} {Zeitraum}") }
    var dateinameFormat by remember { mutableStateOf("Urlaubsschein_{Name}_{Datum}.pdf") }

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showImportMergeDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val success = BackupManager.exportBackup(context, db, it)
                snackbarMessage = if (success) "✓ Backup erfolgreich exportiert!" else "Fehler beim Exportieren des Backups."
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            pendingImportUri = it
            showImportMergeDialog = true
        }
    }

    LaunchedEffect(Unit) {
        empfaengerEmail = appPrefs.empfaengerEmail.first() ?: ""
        betreffFormat = appPrefs.betreffFormat.first()
        dateinameFormat = appPrefs.dateinameFormat.first()
    }

    var blDropdownExpanded by remember { mutableStateOf(false) }
    var isDrawingMode by remember { mutableStateOf(false) }
    var aktuelleGruppe by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var zeichnungPunkte by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    var savedMessage by remember { mutableStateOf(false) }

    val darkModeSetting by themeViewModel.isDarkMode.collectAsState(initial = null)
    val systemDark = isSystemInDarkTheme()
    val isDarkActive = darkModeSetting ?: systemDark

    LaunchedEffect(isDrawingMode) {
        onDrawingModeChanged(isDrawingMode)
    }

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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (showImportMergeDialog) {
        AlertDialog(
            onDismissRequest = { showImportMergeDialog = false },
            title = { Text("Backup importieren", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Möchtest du bestehende Daten mit dem Backup zusammenführen oder sollen bestehende Daten überschrieben werden?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                Button(
                    onClick = {
                        showImportMergeDialog = false
                        pendingImportUri?.let { uri ->
                            scope.launch {
                                val success = BackupManager.importBackup(context, db, uri, merge = true)
                                snackbarMessage = if (success) "✓ Backup erfolgreich zusammengeführt!" else "Fehler: Ungültiges Backup-Format."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Zusammenführen", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportMergeDialog = false
                        pendingImportUri?.let { uri ->
                            scope.launch {
                                val success = BackupManager.importBackup(context, db, uri, merge = false)
                                snackbarMessage = if (success) "✓ Backup erfolgreich importiert (überschrieben)!" else "Fehler: Ungültiges Backup-Format."
                            }
                        }
                    }
                ) {
                    Text("Überschreiben", color = Color.Red)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    GridBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mitarbeiterprofil & Design",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Stammdaten, Unterschrift, E-Mail-Einstellungen, Backup und Design anpassen.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 13.sp
            )

            // --- APP-AKTUALISIERUNG ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("APP-AKTUALISIERUNG", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            (context as? MainActivity)?.triggerManualUpdateCheck()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Nach Update suchen", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- DESIGN / HELL- DUNKELMODUS ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("ERSCHEINUNGSBILD (Hell / Dunkel)", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dunkelmodus aktiv", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                        Switch(
                            checked = isDarkActive,
                            onCheckedChange = { enabled ->
                                themeViewModel.setDarkMode(enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // --- BACKUP & WIEDERHERSTELLUNG ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("DATEN SICHERN & WIEDERHERSTELLEN", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                exportLauncher.launch("urlaubsschein_backup_${LocalDate.now()}.json")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Backup exportieren", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Backup importieren", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (snackbarMessage != null) {
                        Text(
                            text = snackbarMessage!!,
                            color = if (snackbarMessage!!.startsWith("✓")) StatusGreen else Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- E-MAIL & PDF EINSTELLUNGEN ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("LOHNBÜRO & E-MAIL EINSTELLUNGEN", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = empfaengerEmail,
                        onValueChange = { empfaengerEmail = it },
                        label = { Text("E-Mail Lohnbüro") },
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
                        value = betreffFormat,
                        onValueChange = { betreffFormat = it },
                        label = { Text("E-Mail-Betreff Format") },
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
                        value = dateinameFormat,
                        onValueChange = { dateinameFormat = it },
                        label = { Text("PDF-Dateiname Format") },
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

                    Text(
                        "Verfügbare Platzhalter:\n{Name} = Name des Mitarbeiters\n{Zeitraum} = Urlaubszeitraum\n{Datum} = Erstellungsdatum",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
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
                    Text("STAMMDATEN (Verschlüsselt)", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = vorname,
                        onValueChange = { vorname = it },
                        label = { Text("Vorname") },
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
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nachname") },
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
                        value = pnr,
                        onValueChange = { pnr = it },
                        label = { Text("Personalnummer") },
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
                        value = kostenstelle,
                        onValueChange = { kostenstelle = it },
                        label = { Text("Standard-Kostenstelle") },
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
                        value = standardPolier,
                        onValueChange = { standardPolier = it },
                        label = { Text("Standard-Polier") },
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

                    Text("Standard-Bundesland (Baustelle)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(
                            onClick = { blDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${standardBundesland.anzeigename} (${standardBundesland.kuerzel})",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("▼", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                        }

                        DropdownMenu(
                            expanded = blDropdownExpanded,
                            onDismissRequest = { blDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Bundesland.entries.forEach { bl ->
                                DropdownMenuItem(
                                    text = { Text(text = "${bl.anzeigename} (${bl.kuerzel})", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp) },
                                    onClick = {
                                        standardBundesland = bl
                                        blDropdownExpanded = false
                                    },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                )
                            }
                        }
                    }
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
                    Text("UNTERSCHRIFT (Verschlüsselt)", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

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
                                            .height(120.dp)
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
                                                .padding(horizontal = 16.dp, vertical = 4.dp)
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
                        Text("Unterschreibe auf der Linie (Menü ausgeblendet):", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
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
                                        val bmp = Bitmap.createBitmap(canvasSize.width, canvasSize.height, Bitmap.Config.ARGB_8888)
                                        val canvas = android.graphics.Canvas(bmp)
                                        val paint = android.graphics.Paint().apply {
                                            color = android.graphics.Color.BLACK
                                            strokeWidth = 8f
                                            style = android.graphics.Paint.Style.STROKE
                                            strokeCap = android.graphics.Paint.Cap.ROUND
                                            isAntiAlias = true
                                        }
                                        zeichnungPunkte.forEach { gruppe ->
                                            if (gruppe.size > 1) {
                                                val path = android.graphics.Path()
                                                path.moveTo(gruppe[0].x * canvasSize.width, gruppe[0].y * canvasSize.height)
                                                for (i in 1 until gruppe.size) {
                                                    path.lineTo(gruppe[i].x * canvasSize.width, gruppe[i].y * canvasSize.height)
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

            Button(
                onClick = {
                    securePrefs.vorname = vorname
                    securePrefs.name = name
                    securePrefs.personalnummer = pnr
                    securePrefs.kostenstelle = kostenstelle
                    securePrefs.standardPolier = standardPolier
                    securePrefs.standardBundesland = standardBundesland.kuerzel
                    savedMessage = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Profil speichern", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onHelpClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("📖 Hilfe & Bedienungsanleitung", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (savedMessage) {
                Text(
                    text = "✓ Profil erfolgreich verschlüsselt gespeichert!",
                    color = StatusGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
