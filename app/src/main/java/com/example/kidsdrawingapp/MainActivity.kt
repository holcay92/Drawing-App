package com.example.kidsdrawingapp


import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog
import android.os.CountDownTimer
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null

    private var mImageButtonCurrentPaint: ImageButton? = null

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
            customProgressDialogFunction()
            Toast.makeText(this, "Brush size: 5", Toast.LENGTH_SHORT).show()
        }
        val smallBtn = brushDialog.findViewById(R.id.ll_small_brush) as LinearLayout
        smallBtn.setOnClickListener {
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
            customProgressDialogFunction()
            Toast.makeText(this, "Brush size: 10", Toast.LENGTH_SHORT).show()
        }
        val mediumBtn = brushDialog.findViewById(R.id.ll_medium_brush) as LinearLayout
        mediumBtn.setOnClickListener {
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
           customProgressDialogFunction()
            Toast.makeText(this, "Brush size: 20", Toast.LENGTH_SHORT).show()
        }
        val largeBtn = brushDialog.findViewById(R.id.ll_large_brush) as LinearLayout
        largeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
            customProgressDialogFunction()
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

    private fun customDialogFunction() {
        val customDialog = Dialog(this)
       // customDialog.setContentView(R.layout.custom_dialog)
        // customDialog.setContentView(R.layout.custom_dialog)
       /* customDialog.tv_submit.setOnClickListener {
            Toast.makeText(this, "Clicked Submit", Toast.LENGTH_SHORT).show()
            customDialog.dismiss()
        }*/
        /* customDialog.tv_cancel.setOnClickListener {
           Toast.makeText(this, "Clicked Cancel", Toast.LENGTH_SHORT).show()
           customDialog.dismiss()
       }*/
    }

    private fun customProgressDialogFunction() {
        val customProgressDialog = Dialog(this)
        customProgressDialog.setContentView(R.layout.custom_progress_dialog)
        customProgressDialog.show()

        val timer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // do nothing
            }
            override fun onFinish() {
                customProgressDialog.dismiss()
            }
        }
           timer.start()
   }

}