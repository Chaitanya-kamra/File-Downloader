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
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaitanya.filedownloader.R
import com.chaitanya.filedownloader.adapters.ItemAdapter
import com.chaitanya.filedownloader.database.DownloadApp
import com.chaitanya.filedownloader.database.DownloadDao
import com.chaitanya.filedownloader.databinding.ActivityMainBinding
import com.chaitanya.filedownloader.models.DownloadEntity
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
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener,
    WifiReceiver.WifiConnectivityListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetView: View
    private lateinit var wifiReceiver: WifiReceiver

    private var maxParallelDownload: Int = 1

    private var selectedFolder: Uri? = null
    private lateinit var enteredLink: String

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
        val downloadDao =(application as DownloadApp).db.downloadDao()

        lifecycleScope.launch {
            downloadDao.fetchAllDownload().collect {
                Log.d("exactemployee", "$it")
                val list = ArrayList(it)
                setupListOfDataIntoRecyclerView(list,downloadDao)
            }
        }
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
            addDownload(downloadDao)
        }

        val sliderValue = bottomSheetView.findViewById<TextView>(R.id.sliderValue)
        bottomSheetView.findViewById<SeekBar>(R.id.slider)
            .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    sliderValue.text = progress.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // Not needed for this implementation
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // Not needed for this implementation
                }
            })



        bottomSheetView.findViewById<Button>(R.id.addLinkButton).setOnClickListener {
            if (bottomSheetView.findViewById<EditText>(R.id.et_link).text.isNullOrEmpty()) {
                Toast.makeText(this@MainActivity, "Enter Link", Toast.LENGTH_LONG).show()
            } else {
                enteredLink = bottomSheetView.findViewById<EditText>(R.id.et_link).text.toString()
//                fetchUrlDetails("https://ia802607.us.archive.org/27/items/the-kerala-story_202305/BOY%20AND%20THE%20KING%20-%20Abdallah%20Al%20Faisal.jpg")
                fetchUrlDetails(enteredLink)
            }
        }
        bottomSheetView.findViewById<Button>(R.id.errorBackButton).setOnClickListener {
            linkUi()
        }
        bottomSheetView.findViewById<Button>(R.id.settingUpdateButton).setOnClickListener {
            maxParallelDownload = bottomSheetView.findViewById<SeekBar>(R.id.slider).progress
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<Button>(R.id.closeSettingButton).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<Button>(R.id.cancelLinkButton).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<Button>(R.id.cancelDownloadButton).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<Button>(R.id.doneSuccessfulButton).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<Button>(R.id.errorCloseButton).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        binding.buttonAdd.setOnClickListener {
            linkUi()
            bottomSheetDialog.show()
        }
    }

    private fun setupListOfDataIntoRecyclerView(list: ArrayList<DownloadEntity>, downloadDao: DownloadDao) {
        if (list.isNotEmpty()) {

            val itemAdapter = ItemAdapter(list
            ) { deleteId ->
                lifecycleScope.launch {
                    downloadDao.fetchDownloadById(deleteId).collect {
                        deleteRecord(deleteId, downloadDao, it)
                    }
                }

            }
            binding.rvDownloadMain.layoutManager = LinearLayoutManager(this)
//            // adapter instance is set to the recyclerview to inflate the items.
            binding.rvDownloadMain.adapter = itemAdapter
            binding.rvDownloadMain.visibility = View.VISIBLE
            binding.lnlCenter.visibility = View.GONE
        } else {
            binding.rvDownloadMain.visibility = View.GONE
            binding.lnlCenter.visibility = View.VISIBLE
        }
    }

    private fun deleteRecord(deleteId: Int, downloadDao: DownloadDao, downloadEntity : DownloadEntity) {
        lifecycleScope.launch {
            downloadDao.delete(DownloadEntity(deleteId))
            Toast.makeText(
                applicationContext,
                "Record deleted successfully.",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    private fun addDownload(downloadDao: DownloadDao) {
        val name = bottomSheetView.findViewById<EditText>(R.id.etFileName).text.toString()
        val destination = bottomSheetView.findViewById<EditText>(R.id.etDestination).text.toString()
        val extension = bottomSheetView.findViewById<TextView>(R.id.tvExtension).text.toString()
        val size = bottomSheetView.findViewById<TextView>(R.id.tvSize).text.toString()
        val needWifi = bottomSheetView.findViewById<CheckBox>(R.id.wifi_checkbox).isChecked
        val mimeType = getMimeTypeFromExtension(extension)
        val documentFile = DocumentFile.fromTreeUri(this, selectedFolder!!)
        val file = documentFile?.createFile(mimeType!!, "$name.$extension")
        val c = Calendar.getInstance()
        val dateTime = c.time
        val sdf = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
        val date = sdf.format(dateTime)
        Toast.makeText(this, needWifi.toString(), Toast.LENGTH_SHORT).show()
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
        } else if (destination.isEmpty()) {
            Toast.makeText(this, "Select Destination", Toast.LENGTH_SHORT).show()
        } else {
            lifecycleScope.launch {
                try {
                    val downloadEntity = DownloadEntity(
                        downloadUrl = enteredLink,
                        fileName = name,
                        fileType = extension,
                        fileSize = size,
                        fileStatus = "Queue",
                        needWifi = needWifi,
                        fileUri = file?.uri.toString(),
                        date = date
                    )
                    downloadDao.insert(downloadEntity)
                    runOnUiThread{
                        successUi()
                    }
                }catch (e:Exception){
                    runOnUiThread{
                        notSuccessUi()
                    }
                }


            }
        }

                    val intent = Intent(this, DownloadService::class.java)

                    intent.putExtra(DownloadService.EXTRA_IMAGE_URL, enteredLink)
                    ContextCompat.startForegroundService(this, intent)
    }
    fun getMimeTypeFromExtension(fileExtension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
    }

    private fun fetchUrlDetails(urlString: String) {
        runOnUiThread {
            loadingUi()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection()
                connection.connect()

                val fileSize = connection.contentLength
                val fileName = url.path.substring(url.path.lastIndexOf('/') + 1)
                val name = fileName.substringBeforeLast(".")
                val extension = fileName.substringAfterLast(".")
                Log.e("check", fileSize.toString())
                runOnUiThread {
                    downloadUi()
                    val editName = bottomSheetView.findViewById<EditText>(R.id.etFileName)
                    val tvFileSize = bottomSheetView.findViewById<TextView>(R.id.tvSize)
                    val tvExtension = bottomSheetView.findViewById<TextView>(R.id.tvExtension)
                    tvExtension.text = extension
                    tvFileSize.text = if (fileSize < 0) {
                        "Unknown"
                    } else {
                        formatDataSize(fileSize)
                    }
                    editName.setText(name)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Enter Proper Link", Toast.LENGTH_LONG).show()
                    linkUi()
                }
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
                bottomSheetView.findViewById<TextView>(R.id.sliderValue).text =
                    maxParallelDownload.toString()
                bottomSheetView.findViewById<SeekBar>(R.id.slider).progress = maxParallelDownload
                settingUi()
                bottomSheetDialog.show()
                true
            }

            else -> false
        }
    }


    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
        Toast.makeText(this@MainActivity, "fsfs", Toast.LENGTH_LONG).show()
        // Wi-Fi connected, perform the desired functionality here
    }

    private fun settingUi() {
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

    private fun loadingUi() {
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

    private fun linkUi() {
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

    private fun downloadUi() {
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

    private fun successUi() {
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

    private fun notSuccessUi() {
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