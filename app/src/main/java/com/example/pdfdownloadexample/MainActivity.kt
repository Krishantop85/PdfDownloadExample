package com.example.pdfdownloadexample

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.Bitmap

class MainActivity : AppCompatActivity() {

    private val STORAGE_PERMISSION_CODE: Int = 1000
    var bitmap :Bitmap? = null
    var dummyBitmap :Bitmap? = null
    var ticketId = "Sub123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_download.setOnClickListener(View.OnClickListener {
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M ){
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),STORAGE_PERMISSION_CODE)
                }else{
                    createPdf(ticketId) // event id
                }
            }else{

            }
        })
    }

    // create pdf and save external directory
    private fun createPdf(event_id: String){

        val multiFormatWriter = MultiFormatWriter()
        var barcodeEncoder = BarcodeEncoder()
        var bitMatrix: BitMatrix? = null


        val subscriberEmailId = tv_subscriber_name.text.toString()
        val subscriberName = "Tommy"
        val subscriberContact = "8829548707" // to
        val bookingNotes = tv_notes.text.toString()
        val bookingDate = tv_booking_date.text.toString()
        val eventName = tv_event_name.text.toString()
        val eventDateTime = tv_event_date.text.toString()
        val eventLocation = tv_event_location.text.toString()
        val eventSeatCounter = tv_seat_counter.text.toString()
        val eventAmount = btn_price.text.toString()

        val logoBitmap = BitmapFactory.decodeResource(resources,R.drawable.ic_launcher)

        // resize logo
        val resizedLogoBitmap = Bitmap.createScaledBitmap(
            logoBitmap, 128, 128, false
        )

        val document = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(1200, 2010, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val bitmapPaint = Paint()
        val titlePaint = Paint()
        val linePaint = Paint()
        val textPaint = Paint()

        // logo
        canvas.drawBitmap(resizedLogoBitmap, 40F,80F, bitmapPaint)

        // header
        titlePaint.textSize = resources.getDimension(R.dimen._20fs)
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(eventName,220F, 180F, titlePaint )

        // divider
        canvas.drawLine(40F, 248F, 1200-40F, 248F, linePaint)

        // QR Code
        try {
            bitMatrix = multiFormatWriter.encode(event_id, BarcodeFormat.QR_CODE, 240,240)
            val myBitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            canvas.drawBitmap(myBitmap, 40F,290F, bitmapPaint)
        }catch (e: WriterException){
            Log.e("QRCode", "Error: "+e.toString());
            showUserMsg("Error!")
        }

        // Event info
        textPaint.textSize = resources.getDimension(R.dimen._14fs)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Event Name: "+eventName, 300F, 350F, textPaint);
        canvas.drawText("Number of seats: "+eventSeatCounter, 300F, 410F, textPaint);
        canvas.drawText("Amount: "+eventAmount, 300F, 470F, textPaint);
        canvas.drawText("Event Date: "+eventDateTime, 300F, 530F, textPaint);
        canvas.drawText("Location: "+eventLocation, 300F, 590F, textPaint);
        canvas.drawText("Subscriber Name: "+subscriberName, 300F, 650F, textPaint);
        canvas.drawText("Email Id: "+subscriberEmailId, 300F, 710F, textPaint);
        canvas.drawText("Contact: "+subscriberContact, 300F, 770F, textPaint);
        canvas.drawText("Booking Date: "+bookingDate, 300F, 830F, textPaint);

        // Note
        textPaint.textSize = resources.getDimension(R.dimen._14fs)
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("*Note: "+bookingNotes, 40F, 920F, textPaint);

        document.finishPage(page)

        val directoryPath = Environment.getExternalStorageDirectory().path + "/invoices/"

        val dir = File(directoryPath)

        if (!dir.exists())
            dir.mkdirs()
        val filePath: File

        filePath = File(directoryPath, event_id+"_"+eventName+"_tickets.pdf")

        if (filePath.exists()) {
            filePath.delete()
            filePath.createNewFile()
        } else {
            filePath.createNewFile()
        }

        try {
            document.writeTo(FileOutputStream(filePath))
            showUserMsg("Ticket downloaded")
            Log.e("Invoice","Tickets"+filePath)
        }catch (e : IOException) {
            Log.e("Invoice", "Error: "+e.toString());
            showUserMsg("Error!")
        }

        document.writeTo(FileOutputStream(filePath));
        document.close();
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            STORAGE_PERMISSION_CODE ->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    createPdf("Sub123")
                }else{
                    Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun showUserMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    fun getSubscriptionBarCode(event_id: String){

        val multiFormatWriter = MultiFormatWriter()
        var barcodeEncoder = BarcodeEncoder()
        var bitMatrix: BitMatrix? = null

        try {
            bitMatrix = multiFormatWriter.encode(event_id, BarcodeFormat.QR_CODE, 200,200)
            bitmap = barcodeEncoder.createBitmap(bitMatrix)
            iv_bar_code.setImageBitmap(bitmap)
        }catch (e: WriterException){
            Log.e("QRCode", "Error: "+e.toString());
            showUserMsg("Error!")
        }
    }
}
