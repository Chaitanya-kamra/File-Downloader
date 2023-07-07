package com.chaitanya.filedownloader

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.chaitanya.filedownloader.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetDialog : BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMain)
        supportActionBar?.title = ""

        binding.menuMore.setOnClickListener { view ->
            showMenu(view)
        }
        bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        bottomSheetDialog.setContentView(bottomSheetView)



        val slider = bottomSheetView.findViewById<SeekBar>(R.id.slider)
        val sliderValue = bottomSheetView.findViewById<TextView>(R.id.sliderValue)
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sliderValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed for this implementation
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed for this implementation
            }
        })

//        val backButton = bottomSheetView.findViewById<Button>(R.id.backButton)
//        backButton.setOnClickListener {
//            bottomSheetDialog.dismiss()
//        }
//        val addButton = bottomSheetView.findViewById<Button>(R.id.addButton)
//        addButton.setOnClickListener{
//            fetchUrlDetails("http://167.99.95.99/a/10.bin")
//        }
//
//        val updateButton = bottomSheetView.findViewById<Button>(R.id.updateButton)
//        updateButton.setOnClickListener {
//            val parallelDownloadCount = slider.progress
//            // Perform the necessary action to update the parallel download count
//            // You can add your implementation logic here
//        }
        binding.button2.setOnClickListener {
            bottomSheetDialog.show()

        }
    }

    fun fetchUrlDetails(urlString: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.connect()

            val fileSize = connection.contentLength
            val contentType = connection.contentType
            val fileName = url.path.substring(url.path.lastIndexOf('/') + 1)

            // Print or use the fetched details as per your requirements
            println("File Name: $fileName")
            println("File Size: $fileSize bytes")
            println("File Type: $contentType")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showMenu(view: View) {
        PopupMenu(this, view).apply {
            setForceShowIcon(true)
            setOnMenuItemClickListener(this@MainActivity)
            inflate(R.menu.menu_main)
            show()
        }
    }
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this,"setts",Toast.LENGTH_LONG).show()
                true
            }

            else -> false
        }
    }



}