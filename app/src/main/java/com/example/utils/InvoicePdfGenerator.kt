package com.example.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.data.entity.OrderEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoicePdfGenerator {

    fun generatePdf(context: Context, order: OrderEntity): File? {
        val pdfDocument = PdfDocument()
        
        // A4 Specs: 595 x 842 points
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Define colors
        val primaryTeal = 0xFF0E4C4C.toInt()   // Classic Luxury Teal
        val goldenAccent = 0xFFD4AF37.toInt()  // Matte Gold
        val slateText = 0xFF334155.toInt()     // Premium Slate Gray
        val deepCharcoal = 0xFF0F172A.toInt()  // Almost Black for major elements
        val lightGrayBg = 0xFFF8FAFC.toInt()   // Premium Background light gray
        val delicateBorder = 0xFFE2E8F0.toInt()

        // Background
        canvas.drawColor(Color.WHITE)

        val margin = 40f
        val rightMargin = 595f - margin

        // Paints
        val titlePaint = Paint().apply {
            color = primaryTeal
            isAntiAlias = true
            textSize = 22f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val subtitlePaint = Paint().apply {
            color = goldenAccent
            isAntiAlias = true
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.25f
        }

        val headerLabelPaint = Paint().apply {
            color = deepCharcoal
            isAntiAlias = true
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val normalPaint = Paint().apply {
            color = slateText
            isAntiAlias = true
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val boldPaint = Paint().apply {
            color = deepCharcoal
            isAntiAlias = true
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val linePaint = Paint().apply {
            strokeWidth = 1f
            isAntiAlias = true
        }

        // --- DRAW HEADER SECTION ---
        // Brand Full Emblem Visual rendered safely as bitmap to support PDF canvas
        try {
            val brandLogo = context.resources.getDrawable(com.example.R.drawable.ic_aaraksha_logo_full, context.theme)
            val width = 125
            val height = 125
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val logoCanvas = Canvas(bitmap)
            brandLogo.setBounds(0, 0, width, height)
            brandLogo.draw(logoCanvas)
            
            canvas.drawBitmap(bitmap, margin - 10f, 20f, null)
            bitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Brand Coordinates Aligned to Right of Logo
        var currentY = 55f
        val detailsX = margin + 130f

        canvas.drawText("AARAKSHA", detailsX, currentY, titlePaint)
        
        currentY += 14f
        canvas.drawText("RESIN ART STUDIO", detailsX, currentY, subtitlePaint)

        currentY += 22f
        canvas.drawText("Branded Custom Resin Masterpieces & Luxury Handcrafted Homeware", detailsX, currentY, normalPaint)
        
        currentY += 14f
        canvas.drawText("Email: order@aaraksharesin.art  |  Contact: +91 91060 63230", detailsX, currentY, normalPaint)

        // Triple Elegant Divider
        currentY = 145f
        linePaint.color = primaryTeal
        linePaint.strokeWidth = 1.5f
        canvas.drawLine(margin, currentY, rightMargin, currentY, linePaint)
        
        currentY += 4f
        linePaint.color = goldenAccent
        linePaint.strokeWidth = 1f
        canvas.drawLine(margin, currentY, rightMargin, currentY, linePaint)

        // Document Heading
        currentY += 30f
        canvas.drawText("TAX INVOICE & ORDER SPECIFICATION", margin, currentY, headerLabelPaint)

        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val dateCreated = formatter.format(Date(order.createdDate))
        val dateDeadline = formatter.format(Date(order.deliveryDate))

        // --- DRAW TWO-COLUMN INFO SECTION ---
        currentY += 20f
        val boxHeight = 85f
        val colWidth = (595f - 80f) / 2f

        // Solid Backgrounds for detail boxes
        val boxPaint = Paint().apply {
            color = lightGrayBg
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = delicateBorder
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        // Left Box (Client Profile)
        canvas.drawRoundRect(margin, currentY, margin + colWidth - 10f, currentY + boxHeight, 8f, 8f, boxPaint)
        canvas.drawRoundRect(margin, currentY, margin + colWidth - 10f, currentY + boxHeight, 8f, 8f, borderPaint)

        // Right Box (Order Coordinates)
        canvas.drawRoundRect(margin + colWidth + 10f, currentY, rightMargin, currentY + boxHeight, 8f, 8f, boxPaint)
        canvas.drawRoundRect(margin + colWidth + 10f, currentY, rightMargin, currentY + boxHeight, 8f, 8f, borderPaint)

        // Fill Left Box (Client Profile)
        var boxY = currentY + 18f
        canvas.drawText("CUSTOMER / RECIPIENT", margin + 15f, boxY, boldPaint)
        boxY += 16f
        canvas.drawText("Name:   ${order.customerName}", margin + 15f, boxY, normalPaint)
        boxY += 16f
        canvas.drawText("Phone:   ${order.phoneNumber}", margin + 15f, boxY, normalPaint)
        boxY += 16f
        canvas.drawText("Studio Profile: Verified Partner", margin + 15f, boxY, normalPaint)

        // Fill Right Box (Order Coordinates)
        boxY = currentY + 18f
        canvas.drawText("ORDER INFORMATION", margin + colWidth + 25f, boxY, boldPaint)
        boxY += 16f
        canvas.drawText("Reference ID:       #ARK-${order.id}", margin + colWidth + 25f, boxY, normalPaint)
        boxY += 16f
        canvas.drawText("Booking Date:     $dateCreated", margin + colWidth + 25f, boxY, normalPaint)
        boxY += 16f
        canvas.drawText("Estimated Dispatch: $dateDeadline", margin + colWidth + 25f, boxY, normalPaint)

        // --- DRAW SPECIFICATIONS TABLE ---
        currentY += boxHeight + 35f
        canvas.drawText("ARTWORK & PRODUCT DETAILS", margin, currentY, headerLabelPaint)

        currentY += 10f
        // Draw Header of Table
        val tableHeaderY = currentY
        val tableHeaderHeight = 24f
        boxPaint.color = primaryTeal
        canvas.drawRect(margin, tableHeaderY, rightMargin, tableHeaderY + tableHeaderHeight, boxPaint)

        val tableHeaderLabelPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        canvas.drawText("ARTWORK CATEGORY / MODEL", margin + 15f, tableHeaderY + 16f, tableHeaderLabelPaint)
        canvas.drawText("SPECIFIED DIMENSIONS", margin + 220f, tableHeaderY + 16f, tableHeaderLabelPaint)
        canvas.drawText("BUILD STATUS", rightMargin - 110f, tableHeaderY + 16f, tableHeaderLabelPaint)

        currentY += tableHeaderHeight
        // Draw Table Row
        boxPaint.color = 0xFFFFFFFF.toInt()
        val rowHeight = 65f
        canvas.drawRect(margin, currentY, rightMargin, currentY + rowHeight, boxPaint)
        canvas.drawLine(margin, currentY + rowHeight, rightMargin, currentY + rowHeight, borderPaint)

        // Draw Row values
        val textRowY = currentY + 22f
        canvas.drawText(order.productCategory, margin + 15f, textRowY, boldPaint)
        
        val displaySize = if (order.size.isNotEmpty()) order.size else "Not Appended"
        canvas.drawText(displaySize, margin + 220f, textRowY, normalPaint)

        // Custom status badge
        val statusBadgeColor = when (order.orderStatus) {
            "Completed" -> 0xFFD4EDDA.toInt()
            "In Progress" -> 0xFFCCE5FF.toInt()
            "Cancelled" -> 0xFFF8D7DA.toInt()
            else -> 0xFFFFF3CD.toInt()
        }
        val statusTextColor = when (order.orderStatus) {
            "Completed" -> 0xFF155724.toInt()
            "In Progress" -> 0xFF004085.toInt()
            "Cancelled" -> 0xFF721C24.toInt()
            else -> 0xFF856404.toInt()
        }

        val badgePaint = Paint().apply {
            color = statusBadgeColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val badgeTextPaint = Paint().apply {
            color = statusTextColor
            isAntiAlias = true
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawRoundRect(rightMargin - 115f, textRowY - 12f, rightMargin - 15f, textRowY + 4f, 4f, 4f, badgePaint)
        canvas.drawText(order.orderStatus, rightMargin - 105f, textRowY - 1f, badgeTextPaint)

        // Build description details drawn inside row
        canvas.drawText("Description: ${order.description}", margin + 15f, textRowY + 20f, normalPaint)
        if (order.notes.isNotEmpty()) {
            canvas.drawText("Studio Specs: ${order.notes}", margin + 15f, textRowY + 34f, normalPaint)
        }

        // --- DRAW PAYMENT LEDGER SUMMARY ---
        currentY += rowHeight + 35f
        canvas.drawText("PAYMENT LEDGER", margin, currentY, headerLabelPaint)

        currentY += 10f
        val ledgerBoxHeight = 85f
        val ledgerBoxWidth = 240f
        val ledgerX = rightMargin - ledgerBoxWidth

        boxPaint.color = lightGrayBg
        canvas.drawRoundRect(ledgerX, currentY, rightMargin, currentY + ledgerBoxHeight, 8f, 8f, boxPaint)
        canvas.drawRoundRect(ledgerX, currentY, rightMargin, currentY + ledgerBoxHeight, 8f, 8f, borderPaint)

        var ledgerY = currentY + 20f
        canvas.drawText("Total Contract Amount:", ledgerX + 15f, ledgerY, normalPaint)
        canvas.drawText("₹%,.2f".format(order.totalAmount), rightMargin - 15f - boldPaint.measureText("₹%,.2f".format(order.totalAmount)), ledgerY, boldPaint)

        ledgerY += 18f
        canvas.drawText("Advance Deposit Paid:", ledgerX + 15f, ledgerY, normalPaint)
        canvas.drawText("₹%,.2f".format(order.advancePaid), rightMargin - 15f - boldPaint.measureText("₹%,.2f".format(order.advancePaid)), ledgerY, boldPaint)

        ledgerY += 14f
        linePaint.color = delicateBorder
        canvas.drawLine(ledgerX + 15f, ledgerY, rightMargin - 15f, ledgerY, linePaint)

        ledgerY += 18f
        val remaining = order.totalAmount - order.advancePaid
        canvas.drawText("Remaining Balance Due:", ledgerX + 15f, ledgerY, boldPaint)
        
        val finalDueText = "₹%,.2f".format(remaining)
        val finalDuePaint = Paint().apply {
            color = if (remaining > 0) 0xFFDC2626.toInt() else 0xFF155724.toInt()
            isAntiAlias = true
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(finalDueText, rightMargin - 15f - finalDuePaint.measureText(finalDueText), ledgerY, finalDuePaint)

        // Draw Payment Status Badge on Left of Ledger Box
        val pBadgeColor = when (order.paymentStatus) {
            "Settled" -> 0xFFD4EDDA.toInt()
            "Partial" -> 0xFFFFF3CD.toInt()
            else -> 0xFFF8D7DA.toInt()
        }
        val pTextColor = when (order.paymentStatus) {
            "Settled" -> 0xFF155724.toInt()
            "Partial" -> 0xFF856404.toInt()
            else -> 0xFF721C24.toInt()
        }
        badgePaint.color = pBadgeColor
        badgeTextPaint.color = pTextColor
        
        canvas.drawRoundRect(margin, currentY + 12f, margin + 120f, currentY + 45f, 6f, 6f, badgePaint)
        canvas.drawText("Ledger Status:", margin + 12f, currentY + 26f, normalPaint)
        canvas.drawText(order.paymentStatus.uppercase(Locale.ROOT), margin + 12f, currentY + 38f, badgeTextPaint)

        // --- LUXURY BRAND FOOTER ---
        currentY += ledgerBoxHeight + 50f
        
        // Draw gold double dashed lines or linear box for footer
        val footerPaint = Paint().apply {
            color = goldenAccent
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawRect(margin, currentY, rightMargin, currentY + 45f, footerPaint)
        
        val footerTextPaint = Paint().apply {
            color = primaryTeal
            isAntiAlias = true
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
        }
        val footerSubPaint = Paint().apply {
            color = slateText
            isAntiAlias = true
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val thankYouTxt = "Thank you for supporting handcrafted luxury art."
        val handmadeTxt = "Each piece of Aaraksha Resin Art is custom built with premium grade pigments, fluid resin, and master craftsmanship."
        
        canvas.drawText(thankYouTxt, (595f - footerTextPaint.measureText(thankYouTxt)) / 2f, currentY + 18f, footerTextPaint)
        canvas.drawText(handmadeTxt, (595f - footerSubPaint.measureText(handmadeTxt)) / 2f, currentY + 32f, footerSubPaint)

        // Copyright info
        val copyrightTxt = "© ${Date().year + 1900} Aaraksha Resin Art. Created via Aaraksha Companion."
        canvas.drawText(copyrightTxt, (595f - footerSubPaint.measureText(copyrightTxt)) / 2f, 800f, footerSubPaint)

        // Done writing PDF
        pdfDocument.finishPage(page)

        // Save PDF file in cache directory which is fast and supports sharing seamlessly
        val invoiceDir = File(context.cacheDir, "Invoices")
        if (!invoiceDir.exists()) {
            invoiceDir.mkdirs()
        }

        val file = File(invoiceDir, "Invoice_ARK_${order.id}.pdf")
        return try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun savePdfToPublicDownloads(context: Context, pdfFile: File, fileName: String): Boolean {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        java.io.FileInputStream(pdfFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    return true
                }
            } else {
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val destFile = File(downloadsDir, fileName)
                java.io.FileInputStream(pdfFile).use { inputStream ->
                    FileOutputStream(destFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
