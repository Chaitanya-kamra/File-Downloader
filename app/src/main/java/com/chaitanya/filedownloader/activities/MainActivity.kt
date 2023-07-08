package com.chaitanya.filedownloader.activities

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.chaitanya.filedownloader.R
import com.chaitanya.filedownloader.databinding.ActivityMainBinding
import com.chaitanya.filedownloader.utils.DownloadService
import com.chaitanya.filedownloader.utils.WifiReceiver
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener, WifiReceiver.WifiConnectivityListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetDialog : BottomSheetDialog
    private lateinit var bottomSheetView: View
    private lateinit var wifiReceiver: WifiReceiver

    private var maxParallelDownload : Int = 1

    private var selectedFolder: Uri? = null

    // ActivityResultLauncher for folder selection
    private val folderSelectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val folderUri: Uri? = data?.data
                val folderDocument = folderUri?.let { DocumentFile.fromTreeUri(this, it) }
                val folderName = folderDocument?.name
                Log.e("path", folderDocument!!.uri.path.toString())
                selectedFolder = folderUri
                bottomSheetView.findViewById<EditText>(R.id.etDestination).setText(folderName)

            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMain)
        supportActionBar?.title = ""
        wifiReceiver = WifiReceiver(this@MainActivity)
        binding.menuMore.setOnClickListener { view ->
            showMenu(view)
        }
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        bottomSheetDialog.setContentView(bottomSheetView)


        bottomSheetView.findViewById<EditText>(R.id.etDestination).setOnClickListener {
            openFolderSelection()
        }

        bottomSheetView.findViewById<Button>(R.id.addDownloadButton).setOnClickListener {
            val intent = Intent(this, DownloadService::class.java)
            intent.putExtra(DownloadService.EXTRA_IMAGE_URL, "http://167.99.95.99/a/10.bin")
            ContextCompat.startForegroundService(this, intent)
//            downloadVideo("http://167.99.95.99/c/1.bin")
        }


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
        val addButton = bottomSheetView.findViewById<Button>(R.id.addLinkButton)
        addButton.setOnClickListener{
            fetchUrlDetails("https://ia802607.us.archive.org/27/items/the-kerala-story_202305/BOY%20AND%20THE%20KING%20-%20Abdallah%20Al%20Faisal.jpg")
        }


        binding.buttonAdd.setOnClickListener {
            linkUi()
            bottomSheetDialog.show()

        }
    }

    private fun fetchUrlDetails(urlString: String) {
        runOnUiThread{
            loadingUi()
        }
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.connect()


            val fileSize = connection.contentLength
            val fileName = url.path.substring(url.path.lastIndexOf('/') + 1)
            val name = fileName.substringBeforeLast(".")
            val extension = fileName.substringAfterLast(".")
            Log.e("check",fileSize.toString() )
            runOnUiThread{
                downloadUi()
                val editName = bottomSheetView.findViewById<EditText>(R.id.etFileName)
                val tvFileSize = bottomSheetView.findViewById<TextView>(R.id.tvSize)
                val tvExtension = bottomSheetView.findViewById<TextView>(R.id.tvExtension)
                tvExtension.text = extension
                tvFileSize.text = if (fileSize < 0){
                    "Unknown"
                }else {
                    formatDataSize(fileSize)
                }
                editName.setText(name)
            }
        }
    }
    private fun formatDataSize(size: Int): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var fileSize = size.toDouble()
        var unitIndex = 0
        while (fileSize > 1024 && unitIndex < units.size - 1) {
            fileSize /= 1024
            unitIndex++
        }
        return "%.2f %s".format(fileSize, units[unitIndex])
    }

    private fun openFolderSelection() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        folderSelectionLauncher.launch(intent)
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
                settingUi()
                bottomSheetDialog.show()
                true
            }
            else -> false
        }
    }
    private fun downloadVideo(mediaStream: String) {
        mediaStream.let {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(it)
                .header("Connection", "close")
                .build()

//            val titleVid = binding.tvTitle.text.toString()
//            val sanitizedNameVid = titleVid.replace(Regex("[^a-zA-Z0-9.-]"), "")
//            val timestamp = System.currentTimeMillis()
            val fileName = "ok.bin"
            val file = File(filesDir, fileName)
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread { notSuccessUi() }

                    // Handle download failure
                    Log.e("OKHTTP", "failure")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.let { responseBody ->
                        val inputStream = responseBody.byteStream()
                        val outputStream = FileOutputStream(file)

                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        var totalBytesRead: Long = 0
                        val totalBytes = responseBody.contentLength()
                        var progress: Int

                        try {
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead

                                progress = ((totalBytesRead.toDouble() / totalBytes) * 100).toInt()
                                Log.e("progress", progress.toString())
                                // Update the progress bar and display the downloaded and total data
                                runOnUiThread {
//                                    binding.progressBar.progress = progress
//                                    val downloadedData = formatDataSize(totalBytesRead)
//                                    val totalData = formatDataSize(totalBytes)
//                                    binding.tvprogress.text = "$downloadedData / $totalData"
                                }
                            }
                        } catch (e: IOException) {

                            e.printStackTrace()
                            runOnUiThread { notSuccessUi() }
                            Log.e("progress", "failure")
                        } finally {
                            try {
                                outputStream.close()
                                inputStream.close()
                            } catch (e: IOException) {
                                // Handle file close error
                                e.printStackTrace()
                                runOnUiThread { notSuccessUi() }

                            }
                        }
                    }
                }
            })
        }
    }
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnectedOrConnecting == true
        }
    }
    override fun onResume() {
        super.onResume()
        registerWifiReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterWifiReceiver()
    }

    private fun registerWifiReceiver() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(wifiReceiver, filter)
    }

    private fun unregisterWifiReceiver() {
        unregisterReceiver(wifiReceiver)
    }

    override fun onWifiConnected() {
        Toast.makeText(this@MainActivity,"fsfs",Toast.LENGTH_LONG).show()
        // Wi-Fi connected, perform the desired functionality here
    }
    private fun settingUi(){
        val settingLayout = bottomSheetView.findViewById<View>(R.id.lnlSetting)
        val loadingLayout = bottomSheetView.findViewById<View>(R.id.lnlGrabbing)
        val linkLayout = bottomSheetView.findViewById<View>(R.id.lnlEnterLink)
        val downloadLayout = bottomSheetView.findViewById<View>(R.id.lnlAddDownload)
        val successFulLLayout = bottomSheetView.findViewById<View>(R.id.lnlSuccessful)
        val unSuccessLayout = bottomSheetView.findViewById<View>(R.id.lnlNotSuccessFull)
        settingLayout.visibility = View.VISIBLE
        loadingLayout.visibility = View.GONE
        linkLayout.visibility = View.GONE
        downloadLayout.visibility = View.GONE
        successFulLLayout.visibility = View.GONE
        unSuccessLayout.visibility = View.GONE
    }
    private fun loadingUi(){
        val settingLayout = bottomSheetView.findViewById<View>(R.id.lnlSetting)
        val loadingLayout = bottomSheetView.findViewById<View>(R.id.lnlGrabbing)
        val linkLayout = bottomSheetView.findViewById<View>(R.id.lnlEnterLink)
        val downloadLayout = bottomSheetView.findViewById<View>(R.id.lnlAddDownload)
        val successFulLLayout = bottomSheetView.findViewById<View>(R.id.lnlSuccessful)
        val unSuccessLayout = bottomSheetView.findViewById<View>(R.id.lnlNotSuccessFull)
        settingLayout.visibility = View.GONE
        loadingLayout.visibility = View.VISIBLE
        linkLayout.visibility = View.GONE
        downloadLayout.visibility = View.GONE
        successFulLLayout.visibility = View.GONE
        unSuccessLayout.visibility = View.GONE
    }
    private fun linkUi(){
        val settingLayout = bottomSheetView.findViewById<View>(R.id.lnlSetting)
        val loadingLayout = bottomSheetView.findViewById<View>(R.id.lnlGrabbing)
        val linkLayout = bottomSheetView.findViewById<View>(R.id.lnlEnterLink)
        val downloadLayout = bottomSheetView.findViewById<View>(R.id.lnlAddDownload)
        val successFulLLayout = bottomSheetView.findViewById<View>(R.id.lnlSuccessful)
        val unSuccessLayout = bottomSheetView.findViewById<View>(R.id.lnlNotSuccessFull)
        settingLayout.visibility = View.GONE
        loadingLayout.visibility = View.GONE
        linkLayout.visibility = View.VISIBLE
        downloadLayout.visibility = View.GONE
        successFulLLayout.visibility = View.GONE
        unSuccessLayout.visibility = View.GONE
    }
    private fun downloadUi(){
        val settingLayout = bottomSheetView.findViewById<View>(R.id.lnlSetting)
        val loadingLayout = bottomSheetView.findViewById<View>(R.id.lnlGrabbing)
        val linkLayout = bottomSheetView.findViewById<View>(R.id.lnlEnterLink)
        val downloadLayout = bottomSheetView.findViewById<View>(R.id.lnlAddDownload)
        val successFulLLayout = bottomSheetView.findViewById<View>(R.id.lnlSuccessful)
        val unSuccessLayout = bottomSheetView.findViewById<View>(R.id.lnlNotSuccessFull)
        settingLayout.visibility = View.GONE
        loadingLayout.visibility = View.GONE
        linkLayout.visibility = View.GONE
        downloadLayout.visibility = View.VISIBLE
        successFulLLayout.visibility = View.GONE
        unSuccessLayout.visibility = View.GONE
    }
    private fun successUi(){
        val settingLayout = bottomSheetView.findViewById<View>(R.id.lnlSetting)
        val loadingLayout = bottomSheetView.findViewById<View>(R.id.lnlGrabbing)
        val linkLayout = bottomSheetView.findViewById<View>(R.id.lnlEnterLink)
        val downloadLayout = bottomSheetView.findViewById<View>(R.id.lnlAddDownload)
        val successFulLLayout = bottomSheetView.findViewById<View>(R.id.lnlSuccessful)
        val unSuccessLayout = bottomSheetView.findViewById<View>(R.id.lnlNotSuccessFull)
        settingLayout.visibility = View.GONE
        loadingLayout.visibility = View.GONE
        linkLayout.visibility = View.GONE
        downloadLayout.visibility = View.GONE
        successFulLLayout.visibility = View.VISIBLE
        unSuccessLayout.visibility = View.GONE
    }
    private fun notSuccessUi(){
        val settingLayout = bottomSheetView.findViewById<View>(R.id.lnlSetting)
        val loadingLayout = bottomSheetView.findViewById<View>(R.id.lnlGrabbing)
        val linkLayout = bottomSheetView.findViewById<View>(R.id.lnlEnterLink)
        val downloadLayout = bottomSheetView.findViewById<View>(R.id.lnlAddDownload)
        val successFulLLayout = bottomSheetView.findViewById<View>(R.id.lnlSuccessful)
        val unSuccessLayout = bottomSheetView.findViewById<View>(R.id.lnlNotSuccessFull)
        settingLayout.visibility = View.GONE
        loadingLayout.visibility = View.GONE
        linkLayout.visibility = View.GONE
        downloadLayout.visibility = View.GONE
        successFulLLayout.visibility = View.GONE
        unSuccessLayout.visibility = View.VISIBLE
    }

}