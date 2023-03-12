package com.example.kidsdrawingapp

import android.Manifest
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private var customProgressDialog: Dialog? = null
    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null

    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageBackground: ImageView = findViewById(R.id.iv_background)
                imageBackground.setImageURI(result.data?.data)
                drawingView!!.mPaths.clear()
            }
        }
    private val storagePermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                val permission = it.key
                val isGranted = it.value
                if (isGranted) {
                    if (permission == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT)
                            .show()
                        val pickIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(pickIntent)

                    }
                } else {
                    if (permission == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawing_view)
        drawingView!!.setSizeForBrush(20.toFloat())

        val linearLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)
        mImageButtonCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.palette_pressed)
        )

        val ibBrush = findViewById<ImageButton>(R.id.ib_brush)
        ibBrush.setOnClickListener {
            showBrushSizeChooserDialog()
            // Coroutine example with lifecycleScope
            showProgressDialog()
            lifecycleScope.launch { executeCoroutine("Pick a brush size with coroutine!") }
        }
        val ibUndo = findViewById<ImageButton>(R.id.ib_undo)
        ibUndo.setOnClickListener {
            drawingView?.onClickUndo()
        }
        val ibGallery = findViewById<ImageButton>(R.id.ib_gallery)
        ibGallery.setOnClickListener {
            requestStoragePermission()
        }
        val ibSave = findViewById<ImageButton>(R.id.ib_save)
        ibSave.setOnClickListener {
            if (isReadStorageAllowed()) {
                showProgressDialog()
                lifecycleScope.launch {
                    //reference the frame layout
                    val flDrawingView: FrameLayout = findViewById(R.id.fl_drawing_view_container)
                    //Save the image to the device
                    saveBitmapFile(getBitmapFromView(flDrawingView))
                }
            }
        }
    }

    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")

        val xSmallButton = brushDialog.findViewById(R.id.ll_x_small_brush) as LinearLayout
        xSmallButton.setOnClickListener {
            drawingView?.setSizeForBrush(5.toFloat())
            brushDialog.dismiss()
           // customProgressDialogFunction()
            Toast.makeText(this, "Brush size: 5", Toast.LENGTH_SHORT).show()
        }
        val smallBtn = brushDialog.findViewById(R.id.ll_small_brush) as LinearLayout
        smallBtn.setOnClickListener {
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
           // customProgressDialogFunction()
            Toast.makeText(this, "Brush size: 10", Toast.LENGTH_SHORT).show()
        }
        val mediumBtn = brushDialog.findViewById(R.id.ll_medium_brush) as LinearLayout
        mediumBtn.setOnClickListener {
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
            //customProgressDialogFunction()
            Toast.makeText(this, "Brush size: 20", Toast.LENGTH_SHORT).show()
        }
        val largeBtn = brushDialog.findViewById(R.id.ll_large_brush) as LinearLayout
        largeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
            //customProgressDialogFunction()
            // Snackbar.make(findViewById(R.id.drawing_view), "Brush size: 30", Snackbar.LENGTH_SHORT).show()
            Toast.makeText(this, "Brush size: 30", Toast.LENGTH_SHORT).show()
        }
        brushDialog.show()
    }

    fun paintClicked(view: View) {
        if (view !== mImageButtonCurrentPaint) {

            // type cast the view to ImageButton because it can be any view
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()

            // 3rd party library can be used
            drawingView?.setColor(colorTag)

            // update the new palette color background
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_pressed)
            )
            // set the previous color background to normal
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_normal)
            )

            // update the current paint color
            mImageButtonCurrentPaint = view
        }
    }

    private fun alertDialogFunction() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert Dialog")
        builder.setMessage("This is an alert dialog")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes") { _, _ ->
            Toast.makeText(applicationContext, "Clicked yes", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("No") { _, _ ->
            Toast.makeText(applicationContext, "Clicked No", Toast.LENGTH_LONG).show()
        }
        builder.setNeutralButton("Cancel") { _, _ ->
            Toast.makeText(applicationContext, "Clicked Cancel", Toast.LENGTH_LONG).show()
        }
        val alertDialog: AlertDialog = builder.create() // creating alert dialog
        // this prevents the dialog from being dismissed when touched outside the dialog area - empty screen
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun cancelProgressDialog() {
        if (customProgressDialog != null) {
            customProgressDialog!!.dismiss()
            customProgressDialog = null
        }
    }
    private fun showProgressDialog() {
        customProgressDialog = Dialog(this)
        customProgressDialog?.setContentView(R.layout.custom_progress_dialog)
        customProgressDialog?.show()
    }

    private fun customProgressDialogFunction() {
        val customProgressDialog = Dialog(this)
        customProgressDialog.setContentView(R.layout.custom_progress_dialog)
        customProgressDialog.show()

        /* val timer = object : CountDownTimer(3000, 1000) {
              override fun onTick(millisUntilFinished: Long) {
                  // do nothing
              }

              override fun onFinish() {
                  customProgressDialog.dismiss()
              }
          } timer.start()*/

    }

    private fun isReadStorageAllowed(): Boolean {
        // getting the permission status
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        // if permission is granted returning true
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            showRationaleDialog(
                "Storage Permission",
                "This app needs storage permission to access your gallery."
            )
        } else {
            // this is how you request a permission
            storagePermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            // does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String {
        var result = ""
        withContext(Dispatchers.IO) {
            if (mBitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val f = File(
                        externalCacheDir?.absoluteFile.toString() +
                                File.separator + "DrawingApp_" + System.currentTimeMillis() / 1000 + ".png"
                    )
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath

                    runOnUiThread {
                        cancelProgressDialog()
                        if (result.isNotEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "File saved successfully: $result",
                                Toast.LENGTH_SHORT
                            ).show()
                            shareImage(result)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "File can not be saved",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: java.lang.Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    private suspend fun executeCoroutine(message: String) {
        withContext(Dispatchers.IO) {
            for (i in 0..100000) {
                Log.d("delay", "Loop -> $i")
            }

            // this will work because this function is called from the main thread
            runOnUiThread {
                cancelProgressDialog()
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }


            // this wont work on this thread because we are not on the main thread
            // Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRationaleDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                // Request the permission
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun shareImage(result:String) {
        MediaScannerConnection.scanFile(this, arrayOf(result), null) {
                path, uri ->
                val shareIntent = Intent()
            //intent type action send
                shareIntent.action = Intent.ACTION_SEND

                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            // this is the type of data that we are sending
                shareIntent.type = "image/png"
            // this opens the share dialog
                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        "Share"
                    )
                )
        }

    }
}