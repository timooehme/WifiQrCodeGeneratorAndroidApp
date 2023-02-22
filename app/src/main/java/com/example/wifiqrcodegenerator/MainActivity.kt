package com.example.wifiqrcodegenerator

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.wifiqrcodegenerator.databinding.ActivityMainBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** connecting UI to functions*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(binding.root)
        binding.buttonSubmit.setOnClickListener { createQR() }
        binding.imageViewQRWelcome.setImageBitmap(qrbitcreator("Welcome to the WifiQrCodeGenerator"))
        binding.floatingActionButton.setOnClickListener { saveQR() }
    }
    /** Helper function for saving*/
    private fun saveQR() {
        createQR()
        val strcomplete =
            "WIFI:T:WPA;S:${binding.TESSID.editText?.text};P:${binding.TEpassword.editText?.text};H:false;;"
        val bitmap = (qrbitcreator(strcomplete))
        saveMediaToStorage(bitmap)

    }

    /** Helper function for displaying*/
    private fun createQR() {
        if (TextUtils.isEmpty(binding.TESSID.editText?.text.toString()) || TextUtils.isEmpty(binding.TEpassword.editText?.text.toString())) {
            Toast.makeText(applicationContext, "Enter your SSID and Password", Toast.LENGTH_SHORT)
                .show()
        } else {
            val strcomplete =
                "WIFI:T:WPA;S:${binding.TESSID.editText?.text};P:${binding.TEpassword.editText?.text};H:false;;"
            binding.imageViewQRWelcome.setImageBitmap(qrbitcreator(strcomplete))

        }
    }

/** Creates the QR Code*/
    private fun qrbitcreator(strcomplete: String): Bitmap {
        val size = 370
        val bits = QRCodeWriter().encode(strcomplete, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
    /** Saves the QR Code to the Android Photo gallery*/
    private fun saveMediaToStorage(bitmap: Bitmap) {
        //Generating a file name
        val filename =
            "${binding.TESSID.editText?.text.toString()}WIFI_QR_CODE_${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream?

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            contentResolver.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values to make sure it will added in front of gallery
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //For devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            //writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(applicationContext, "Saved to Photos", Toast.LENGTH_SHORT)
                .show()
        }
    }
}