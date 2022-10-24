package com.ix.imagex.ui

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.ix.imagex.R
import com.ix.imagex.utils.ImageUtils
import com.ix.imagex.utils.ImageUtils.imageByte
import com.ix.imagex.utils.ImageUtils.write


class MainActivity : AppCompatActivity() {


    private val imageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            try {
                ImageUtils.uriToByte(this, it)
                println(imageByte?.size)
                val yesOrNo = findViewById<ImageView>(R.id.image_yes_or_no)
                yesOrNo.setImageDrawable(ResourcesCompat.getDrawable(this.resources, R.drawable.ic_yes, theme))
            } catch (e: Exception) {
                Log.w("Select Error", e)
            }
        }
    }

    private val imageWriter = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
        if (it != null) {
            val groupRate = findViewById<RadioGroup>(R.id.group_rate)
            val groupFormat = findViewById<RadioGroup>(R.id.group_format)
            val rate = (findViewById<RadioButton>(groupRate.checkedRadioButtonId).text.toString()
                .substringBefore("%")).toInt() / 100.0
            val format = findViewById<RadioButton>(groupFormat.checkedRadioButtonId).text.toString()
            val quality = findViewById<SeekBar>(R.id.seekBar).progress
            alertDialogBuilder.setView(R.layout.main_loading).setCancelable(false)
            val progressDialog = alertDialogBuilder.create()
            progressDialog.show()
            object : Thread() {
                override fun run() {
                    write(rate, quality, format, it, this@MainActivity)
                    progressDialog.dismiss()
                    Looper.prepare()
                    Toast.makeText(
                        this@MainActivity, getString(R.string.generate_success), Toast.LENGTH_LONG
                    ).show()
                    Looper.loop()
                }
            }.start()
        } else {
            Toast.makeText(
                this, getString(R.string.generate_error), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val alertDialogBuilder by lazy { AlertDialog.Builder(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val qualityLabel = findViewById<TextView>(R.id.quality)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)

        var format = ".jpeg"
        findViewById<RadioGroup>(R.id.group_format).setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radio_png) {
                seekBar.isEnabled = false
                seekBar.progress = 10
                format = ".png"
            } else {
                seekBar.isEnabled = true
                format = ".jpeg"
            }
        }

        qualityLabel.text = String.format(getString(R.string.quality, (seekBar.progress + 2).toString()))

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                qualityLabel.text = String.format(getString(R.string.quality, (progress + 2).toString()))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        findViewById<Button>(R.id.btn_select).setOnClickListener {
            imageLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.btn_make).setOnClickListener {
            if (imageByte == null) {
                Toast.makeText(
                    this, getString(R.string.select_image), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val dictChars =
                mutableListOf<Char>().apply { "1234567890qwertyuiopasdfghjklzxcvbnm".forEach { this.add(it) } }
            val randomStr = StringBuilder().apply {
                (5..(10..15).random()).onEach { append(dictChars.random()) }
            }.append(format)
            imageWriter.launch(randomStr.toString())
        }


    }

}