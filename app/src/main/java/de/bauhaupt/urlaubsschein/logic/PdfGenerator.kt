package de.bauhaupt.urlaubsschein.logic

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import de.bauhaupt.urlaubsschein.data.repository.AppPreferencesRepository
import de.bauhaupt.urlaubsschein.data.security.SecurePreferences
import de.bauhaupt.urlaubsschein.ui.wizard.WizardUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale

object PdfGenerator {

    private const val ABSTAND_NACH_LABEL = " "
    private const val DATUM_LINIEN_LAENGE = 70f
    private const val PERS_NR_LINIEN_LAENGE = 60f

    fun generatePdf(context: Context, state: WizardUiState, signatureFile: File?): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val securePrefs = SecurePreferences(context)
        val employeeName = "${securePrefs.name}, ${securePrefs.vorname}"
        val employeePnr = securePrefs.personalnummer

        val appPrefs = AppPreferencesRepository(context)
        val dateinameFormat = runBlocking { appPrefs.dateinameFormat.first() }

        val datumVal = state.vonDatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val dateinameRaw = dateinameFormat
            .replace("{Name}", securePrefs.name.replace(" ", "").ifBlank { "Mitarbeiter" })
            .replace("{Zeitraum}", "${state.vonDatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}_${state.bisDatum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
            .replace("{Datum}", datumVal)

        val fileName = if (dateinameRaw.endsWith(".pdf", ignoreCase = true)) dateinameRaw else "$dateinameRaw.pdf"

        // --- SCHRIFTARTEN ---
        val fontBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val fontNormal = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        val p16Bold = Paint().apply { typeface = fontBold; textSize = 16f; color = Color.BLACK; isAntiAlias = true }
        val p12Normal = Paint().apply { typeface = fontNormal; textSize = 12f; color = Color.BLACK; isAntiAlias = true }
        val p10Normal = Paint().apply { typeface = fontNormal; textSize = 10f; color = Color.BLACK; isAntiAlias = true }
        val p26Bold = Paint().apply { typeface = fontBold; textSize = 26f; color = Color.BLACK; isAntiAlias = true }
        val p14Bold = Paint().apply { typeface = fontBold; textSize = 14f; color = Color.BLACK; isAntiAlias = true }

        val linePaint = Paint().apply { color = Color.BLACK; strokeWidth = 0.8f; isAntiAlias = true }
        val boxPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 1f; isAntiAlias = true }
        val checkPaint = Paint().apply { color = Color.BLACK; strokeWidth = 1.5f; isAntiAlias = true; strokeCap = Paint.Cap.ROUND }

        val rightEdge = 794f
        val dreiLeerzeichenBreite = p12Normal.measureText("   ")

        // --- KOPFBEREICH ---
        canvas.drawText("Mustermann Baugesellschaft mbH", 50f, 40f, p16Bold)
        canvas.drawText("Hochbau und konstruktiver Ingenieurbau", 50f, 55f, p12Normal)
        canvas.drawText("Musterstrasse 17, 12345 Musterstadt", 50f, 67f, p10Normal)
        canvas.drawText("Fernruf 01234-567890", 50f, 79f, p10Normal)
        canvas.drawText("U R L A U B S A N T R A G", 350f, 78f, p26Bold)

        // --- LOGO ---
        drawLogoAsVector(canvas)

        // --- SPALTEN-BERECHNUNG FÜR ZEILE 1 & 2 ---
        val col1LabelX = 50f

        // 1. Datum Block (Fix bei 525f)
        val datumLineStart = 525f
        val datumLineEnd = datumLineStart + DATUM_LINIEN_LAENGE

        // 2. Pers.Nr. & Kst.-St. Block: Endet mit 3 Leerzeichen Abstand vor der Datumslinie
        val persNrLineEnd = datumLineStart - dreiLeerzeichenBreite
        val persNrLineStart = persNrLineEnd - PERS_NR_LINIEN_LAENGE

        val maxLabelWidth = maxOf(
            p12Normal.measureText("Pers.Nr.:"),
            p12Normal.measureText("Kst.-St.:")
        )
        val col2LabelX = persNrLineStart - maxLabelWidth - p12Normal.measureText(ABSTAND_NACH_LABEL)

        // 3. Name & Baustelle Linien: Enden mit 3 Leerzeichen Abstand vor dem Pers.Nr.-Block
        val col1LineEnd = col2LabelX - dreiLeerzeichenBreite

        val nameVornameLabel = "Name, Vorname:"
        val nameVornameLineStart = col1LabelX + p12Normal.measureText(nameVornameLabel + ABSTAND_NACH_LABEL)

        val baustelleLabel = "Baustelle:"
        val baustelleLineStart = col1LabelX + p12Normal.measureText(baustelleLabel + ABSTAND_NACH_LABEL)

        // --- UNTERSCHRIFTEN-LINIE (Zeile 1) ---
        val unterschriftLineStart = datumLineEnd + dreiLeerzeichenBreite

        // --- GENEHMIGT-BLOCK ---
        val blockLeft = (persNrLineStart + persNrLineEnd) / 2f
        val blockWidth = rightEdge - blockLeft
        val colWidth = blockWidth / 3f

        val col1X = blockLeft
        val col2X = blockLeft + colWidth
        val col3X = blockLeft + (colWidth * 2f)

        val approvalLineLength = colWidth - dreiLeerzeichenBreite

        // --- ZEILE 1: Name, Pers.Nr, Datum, Unterschrift ---

        // 1. Name, Vorname (aus Stammdaten)
        canvas.drawText(nameVornameLabel, col1LabelX, 170f, p12Normal)
        canvas.drawLine(nameVornameLineStart, 173f, col1LineEnd, 173f, linePaint)
        drawCenteredText(canvas, employeeName, nameVornameLineStart, col1LineEnd, 168f, p14Bold)

        // 2. Pers.Nr. (aus Stammdaten)
        val persNrLabel = "Pers.Nr.:"
        canvas.drawText(persNrLabel, col2LabelX, 170f, p12Normal)
        canvas.drawLine(persNrLineStart, 173f, persNrLineEnd, 173f, linePaint)
        drawCenteredText(canvas, employeePnr, persNrLineStart, persNrLineEnd, 168f, p14Bold)

        // 3. Datum
        canvas.drawLine(datumLineStart, 173f, datumLineEnd, 173f, linePaint)
        val datumLabel = "(Datum)"
        val datumLabelX = datumLineStart + (DATUM_LINIEN_LAENGE / 2f) - (p10Normal.measureText(datumLabel) / 2f)
        canvas.drawText(datumLabel, datumLabelX, 185f, p10Normal)
        drawCenteredText(canvas, java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yy")), datumLineStart, datumLineEnd, 168f, p14Bold)

        // 4. Unterschrift
        canvas.drawLine(unterschriftLineStart, 173f, rightEdge, 173f, linePaint)
        val unterschriftLabel = "(Unterschrift)"
        val unterschriftLabelX = unterschriftLineStart + ((rightEdge - unterschriftLineStart) / 2) - (p10Normal.measureText(unterschriftLabel) / 2)
        canvas.drawText(unterschriftLabel, unterschriftLabelX, 185f, p10Normal)

        // --- ZEILE 2: Baustelle, Kst. ---

        // 1. Baustelle
        canvas.drawText(baustelleLabel, col1LabelX, 212f, p12Normal)
        canvas.drawLine(baustelleLineStart, 215f, col1LineEnd, 215f, linePaint)
        drawCenteredText(canvas, state.baustelle, baustelleLineStart, col1LineEnd, 210f, p14Bold)

        // 2. Kst.-St.
        val kstStLabel = "Kst.-St.:"
        canvas.drawText(kstStLabel, col2LabelX, 212f, p12Normal)
        canvas.drawLine(persNrLineStart, 215f, persNrLineEnd, 215f, linePaint)
        drawCenteredText(canvas, state.kostenstelle, persNrLineStart, persNrLineEnd, 210f, p14Bold)

        // --- GENEHMIGUNGSVERMERKE (Höher gesetzt) ---
        canvas.drawText("befürwortet", col1X, 250f, p10Normal)
        canvas.drawText("Polier", col1X, 268f, p12Normal)
        canvas.drawText("Bauleiter", col2X, 268f, p12Normal)
        canvas.drawText("genehmigt", col3X, 250f, p10Normal)
        canvas.drawText("Abteilungsleiter", col3X, 268f, p12Normal)

        // --- ZEILEN: Alle 4 Zeilen (3x Tarifurlaub + 1x Sonderurlaub) werden IMMER dargestellt ---
        val zeilenY = listOf(315f, 365f, 415f, 485f)

        val xLinie1Start = 190f
        val xLinie1End = 250f
        val xBis = 260f
        val xLinie2Start = 290f
        val xLinie2End = 350f
        val xGleich = 360f
        val xLinie3Start = 380f
        val xLinie3End = 410f
        val xTage = 420f

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")
        val istSonderurlaub = state.urlaubsArt == "Sonderurlaub"

        // 1. Zeichne alle 3 Tarifurlaub-Zeilen
        for (i in 0 until 3) {
            val y = zeilenY[i]
            canvas.drawRect(50f, y - 17f, 70f, y + 3f, boxPaint)
            canvas.drawText("Tarifurlaub vom", 75f, y - 3f, p12Normal)
            canvas.drawLine(xLinie1Start, y, xLinie1End, y, linePaint)
            canvas.drawText("bis", xBis, y - 3f, p12Normal)
            canvas.drawLine(xLinie2Start, y, xLinie2End, y, linePaint)
            canvas.drawText("=", xGleich, y - 3f, p12Normal)
            canvas.drawLine(xLinie3Start, y, xLinie3End, y, linePaint)
            canvas.drawText("Tage", xTage, y - 3f, p12Normal)

            // Genehmigungslinien für diese Zeile
            canvas.drawLine(col1X, y, col1X + approvalLineLength, y, linePaint)
            canvas.drawLine(col2X, y, col2X + approvalLineLength, y, linePaint)
            canvas.drawLine(col3X, y, rightEdge, y, linePaint)

            if (!istSonderurlaub && i < state.urlaubsPerioden.size) {
                val periode = state.urlaubsPerioden[i]
                canvas.drawLine(54f, y - 13f, 66f, y - 1f, checkPaint)
                canvas.drawLine(66f, y - 13f, 54f, y - 1f, checkPaint)

                drawCenteredText(canvas, periode.von.format(formatter), xLinie1Start, xLinie1End, y - 5f, p14Bold)
                drawCenteredText(canvas, periode.bis.format(formatter), xLinie2Start, xLinie2End, y - 5f, p14Bold)
                drawCenteredText(canvas, periode.tage.toString(), xLinie3Start, xLinie3End, y - 5f, p14Bold)
            }
        }

        // 2. Zeichne die Sonderurlaub-Zeile (Zeile 4)
        val ySonder = zeilenY[3]
        canvas.drawRect(50f, ySonder - 17f, 70f, ySonder + 3f, boxPaint)
        canvas.drawText("Sonderurlaub vom", 75f, ySonder - 3f, p12Normal)
        canvas.drawLine(xLinie1Start, ySonder, xLinie1End, ySonder, linePaint)
        canvas.drawText("bis", xBis, ySonder - 3f, p12Normal)
        canvas.drawLine(xLinie2Start, ySonder, xLinie2End, ySonder, linePaint)
        canvas.drawText("=", xGleich, ySonder - 3f, p12Normal)
        canvas.drawLine(xLinie3Start, ySonder, xLinie3End, ySonder, linePaint)
        canvas.drawText("Tage", xTage, ySonder - 3f, p12Normal)

        // Genehmigungslinien für Sonderurlaub
        canvas.drawLine(col1X, ySonder, col1X + approvalLineLength, ySonder, linePaint)
        canvas.drawLine(col2X, ySonder, col2X + approvalLineLength, ySonder, linePaint)
        canvas.drawLine(col3X, ySonder, rightEdge, ySonder, linePaint)

        if (istSonderurlaub) {
            canvas.drawLine(54f, ySonder - 13f, 66f, ySonder - 1f, checkPaint)
            canvas.drawLine(66f, ySonder - 13f, 54f, ySonder - 1f, checkPaint)

            drawCenteredText(canvas, state.vonDatum.format(formatter), xLinie1Start, xLinie1End, ySonder - 5f, p14Bold)
            drawCenteredText(canvas, state.bisDatum.format(formatter), xLinie2Start, xLinie2End, ySonder - 5f, p14Bold)
            drawCenteredText(canvas, state.berechneteTage.toString(), xLinie3Start, xLinie3End, ySonder - 5f, p14Bold)
        }

        // Grund-Zeile
        canvas.drawText("Grund:  ", 75f, 522f, p12Normal)
        val grundLineStart = 75f + p12Normal.measureText("Grund:  ")
        val grundLineEnd = xTage
        canvas.drawLine(grundLineStart, 525f, grundLineEnd, 525f, linePaint)

        if (state.sonderurlaubGrund.isNotBlank()) {
            drawCenteredText(canvas, state.sonderurlaubGrund, grundLineStart, grundLineEnd, 520f, p12Normal)
        }

        // --- UNTERSCHRIFT ---
        val schreibLinieY = 173f
        drawSignatureRobust(context, canvas, signatureFile, unterschriftLineStart, rightEdge, 100f, schreibLinieY, securePrefs.signatureNormY, p14Bold)

        // --- ZUSATZ-INFOS ---
        val pStatus = Paint().apply { typeface = fontBold; textSize = 12f; color = Color.rgb(34, 197, 94); isAntiAlias = true; textAlign = Paint.Align.RIGHT }
        canvas.drawText("STATUS: EINGEREICHT", rightEdge, 580f, pStatus)

        val pReturn = Paint().apply { typeface = fontNormal; textSize = 10f; color = Color.BLACK; isAntiAlias = true }
        canvas.drawText("erster Arbeitstag nach Urlaub:", 50f, 580f, pReturn)

        val rueckkehrFormatter = DateTimeFormatter.ofPattern("EEE, dd.MM.yyyy", Locale.GERMAN)
        canvas.drawText(state.rueckkehrDatum.format(rueckkehrFormatter), 215f, 580f, pReturn)

        document.finishPage(page)

        // --- SPEICHERN ---
        val tempFile = File(context.cacheDir, fileName)
        try {
            document.writeTo(FileOutputStream(tempFile))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    tempFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        } else {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val destFile = File(documentsDir, fileName)
            tempFile.copyTo(destFile, overwrite = true)
        }

        return tempFile
    }

    private fun drawCenteredText(
        canvas: Canvas,
        text: String,
        lineStart: Float,
        lineEnd: Float,
        textY: Float,
        originalPaint: Paint
    ) {
        val lineWidth = lineEnd - lineStart
        val textWidth = originalPaint.measureText(text)

        val paintToUse = if (textWidth > lineWidth) {
            val scaleFactor = lineWidth / textWidth
            val newPaint = Paint(originalPaint)
            val newSize = maxOf(originalPaint.textSize * scaleFactor, 8f)
            newPaint.textSize = newSize
            newPaint
        } else {
            originalPaint
        }

        val centeredX = lineStart + (lineWidth / 2) - (paintToUse.measureText(text) / 2)
        canvas.drawText(text, centeredX, textY, paintToUse)
    }

    private fun drawSignatureRobust(
        context: Context,
        canvas: Canvas,
        signatureFile: File?,
        cellStartX: Float,
        cellEndX: Float,
        cellTopY: Float,
        schreibLinieY: Float,
        schreibLinieYNorm: Float,
        fallbackPaint: Paint
    ) {
        if (signatureFile == null || !signatureFile.exists()) {
            drawFallbackSignature(canvas, cellStartX, cellEndX, cellTopY, schreibLinieY, fallbackPaint)
            return
        }

        val bitmap = BitmapFactory.decodeFile(signatureFile.absolutePath)
        if (bitmap == null) {
            drawFallbackSignature(canvas, cellStartX, cellEndX, cellTopY, schreibLinieY, fallbackPaint)
            return
        }

        val tintenRect = Rect()
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var minX = bitmap.width
        var minY = bitmap.height
        var maxX = 0
        var maxY = 0
        var hasPixels = false

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = pixels[y * bitmap.width + x]
                if (Color.alpha(pixel) > 0) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                    hasPixels = true
                }
            }
        }

        if (!hasPixels) {
            drawFallbackSignature(canvas, cellStartX, cellEndX, cellTopY, schreibLinieY, fallbackPaint)
            return
        }

        tintenRect.set(minX, minY, maxX + 1, maxY + 1)

        val cellWidth = cellEndX - cellStartX
        val cellHeight = schreibLinieY - cellTopY
        val tintenWidth = tintenRect.width().toFloat()
        val tintenHeight = tintenRect.height().toFloat()
        val aspectRatio = tintenWidth / tintenHeight

        val securePrefs = SecurePreferences(context)
        val sigScale = securePrefs.signatureScale

        var targetWidth = cellWidth * sigScale
        var targetHeight = targetWidth / aspectRatio
        if (targetHeight > cellHeight * sigScale) {
            targetHeight = cellHeight * sigScale
            targetWidth = targetHeight * aspectRatio
        }

        val left = cellStartX + (cellWidth - targetWidth) / 2
        val top = schreibLinieY - (targetHeight * schreibLinieYNorm) - (targetHeight * 0.18f)
        val destRect = RectF(left, top, left + targetWidth, top + targetHeight)

        val paint = Paint().apply {
            isAntiAlias = true
            colorFilter = android.graphics.PorterDuffColorFilter(
                Color.rgb(0, 0, 139),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        canvas.drawBitmap(bitmap, tintenRect, destRect, paint)
    }

    private fun drawFallbackSignature(
        canvas: Canvas,
        cellStartX: Float,
        cellEndX: Float,
        cellTopY: Float,
        schreibLinieY: Float,
        paint: Paint
    ) {
        val text = "gez. Max Mustermann"
        val textWidth = paint.measureText(text)
        val x = cellStartX + ((cellEndX - cellStartX) - textWidth) / 2
        val y = schreibLinieY - 5f
        canvas.drawText(text, x, y, paint)
    }

    private fun drawLogoAsVector(canvas: Canvas) {
        val logoPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val size = 42f
        val startX = 794f - size
        val startY = 32f
        val scale = size / 360f

        logoPaint.color = Color.rgb(29, 29, 27)
        canvas.drawRect(startX, startY, startX + size, startY + size, logoPaint)

        logoPaint.color = Color.rgb(255, 237, 0)
        val dachPath = Path().apply {
            moveTo(startX + 32.5f * scale, startY + 76.5f * scale)
            lineTo(startX + 180.5f * scale, startY + 30f * scale)
            lineTo(startX + 328f * scale, startY + 76.5f * scale)
            lineTo(startX + 328f * scale, startY + 87f * scale)
            lineTo(startX + 180.5f * scale, startY + 40f * scale)
            lineTo(startX + 32.5f * scale, startY + 87f * scale)
            close()
        }
        canvas.drawPath(dachPath, logoPaint)

        val innerDachPath = Path().apply {
            moveTo(startX + 56f * scale, startY + 87f * scale)
            lineTo(startX + 180.5f * scale, startY + 48f * scale)
            lineTo(startX + 305f * scale, startY + 87f * scale)
            close()
        }
        canvas.drawPath(innerDachPath, logoPaint)

        canvas.drawRect(startX + 32.5f * scale, startY + 91.5f * scale, startX + 328f * scale, startY + 101.5f * scale, logoPaint)
        canvas.drawRect(startX + 52.5f * scale, startY + 112f * scale, startX + 308.5f * scale, startY + 122.5f * scale, logoPaint)
        canvas.drawRect(startX + 74.5f * scale, startY + 139.5f * scale, startX + 119f * scale, startY + 262f * scale, logoPaint)

        val mitte1 = Path().apply {
            moveTo(startX + 133.5f * scale, startY + 139.5f * scale)
            lineTo(startX + 176f * scale, startY + 139.5f * scale)
            lineTo(startX + 133.5f * scale, startY + 189f * scale)
            close()
        }
        canvas.drawPath(mitte1, logoPaint)

        val mitte2 = Path().apply {
            moveTo(startX + 133.5f * scale, startY + 204.5f * scale)
            lineTo(startX + 158f * scale, startY + 204.5f * scale)
            cubicTo(
                startX + 166f * scale, startY + 204.5f * scale,
                startX + 171.5f * scale, startY + 212f * scale,
                startX + 171.5f * scale, startY + 221f * scale
            )
            lineTo(startX + 171.5f * scale, startY + 262f * scale)
            lineTo(startX + 133.5f * scale, startY + 262f * scale)
            close()
        }
        canvas.drawPath(mitte2, logoPaint)

        canvas.drawRect(startX + 187f * scale, startY + 139.5f * scale, startX + 231f * scale, startY + 262f * scale, logoPaint)

        val rechts1 = Path().apply {
            moveTo(startX + 243.5f * scale, startY + 183f * scale)
            lineTo(startX + 287f * scale, startY + 183f * scale)
            lineTo(startX + 287f * scale, startY + 262f * scale)
            close()
        }
        canvas.drawPath(rechts1, logoPaint)

        val rechts2 = Path().apply {
            moveTo(startX + 236.5f * scale, startY + 196f * scale)
            lineTo(startX + 272.5f * scale, startY + 176f * scale)
            lineTo(startX + 272.5f * scale, startY + 216f * scale)
            close()
        }
        canvas.drawPath(rechts2, logoPaint)

        val rechts3 = Path().apply {
            moveTo(startX + 287f * scale, startY + 211f * scale)
            lineTo(startX + 287f * scale, startY + 262f * scale)
            lineTo(startX + 235f * scale, startY + 262f * scale)
            close()
        }
        canvas.drawPath(rechts3, logoPaint)

        canvas.drawRect(startX + 52.5f * scale, startY + 277f * scale, startX + 308.5f * scale, startY + 287.5f * scale, logoPaint)
        canvas.drawRect(startX + 32.5f * scale, startY + 298f * scale, startX + 328f * scale, startY + 308.5f * scale, logoPaint)
        canvas.drawRect(startX + 32.5f * scale, startY + 319f * scale, startX + 328f * scale, startY + 329.5f * scale, logoPaint)
    }
}
