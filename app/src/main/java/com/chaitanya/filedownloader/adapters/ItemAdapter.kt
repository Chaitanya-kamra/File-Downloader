package com.chaitanya.filedownloader.adapters

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chaitanya.filedownloader.R
import com.chaitanya.filedownloader.databinding.ItemDownloadBinding
import com.chaitanya.filedownloader.models.DownloadEntity
import com.chaitanya.filedownloader.utils.DownloadService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class ItemAdapter(
    private val items: ArrayList<DownloadEntity>,
    private val deleteListener: (id: Int) -> Unit
) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    class ViewHolder(binding: ItemDownloadBinding) : RecyclerView.ViewHolder(binding.root) {

        val tvTitle = binding.itemTitle
        val tvProgress = binding.tvProgress
        val tvStatus = binding.tvItemStatus
        val progressBar = binding.itemProgress
        val ivIcon = binding.ivIcon
        val ivDelete = binding.ivCross
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDownloadBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = items[position]
        val title = "${item.fileName}.${item.fileType}"

        holder.tvTitle.text = title

        if (item.fileStatus != "Completed") {
            if (item.fileStatus == "WaitWifi") {
                holder.tvProgress.visibility = View.VISIBLE
                holder.tvProgress.text = "${item.progress}%"
                holder.progressBar.progress = item.progress
                holder.tvStatus.text = "Waiting for WiFi"
                holder.ivIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.queue))
                holder.progressBar.progressTintList =
                    ColorStateList.valueOf(Color.parseColor("#FBC441"))
                if (item.isPaused) {
                    //TODO
                } else {

                }
            } else {
                if (item.fileStatus == "Queue") {

                    holder.tvProgress.visibility = View.INVISIBLE
                    holder.progressBar.progress = item.progress
                    holder.tvStatus.text = "Waiting  in queue"
                    holder.ivIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.queue
                        )
                    )
                } else {
                    if (item.fileStatus == "Running") {
                        val numberPattern = "\\d+".toRegex()

                        val matchResult = numberPattern.find(item.fileSize.toString())
                        val number = matchResult?.value?.toIntOrNull()
                        val progressI = (number!!*(item.progress.toDouble()/100)).toInt()
                        val unit = item.fileSize!!.substringAfter(matchResult?.value ?: "")

                        holder.tvProgress.visibility = View.VISIBLE
                        holder.progressBar.progress = item.progress
                        holder.tvProgress.text = "${item.progress}%"
                        holder.tvStatus.text = "$progressI$unit/${item.fileSize}"
                        holder.ivIcon.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.pause
                            )
                        )
                        holder.itemView.setOnClickListener {
                            GlobalScope.launch {
                                val intent = Intent(context, DownloadService::class.java)

                                intent.putExtra(DownloadService.ACTION_PAUSE_DOWNLOAD, item)
                                ContextCompat.startForegroundService(context, intent)
                            }
                        }
                    }else{
                    if (item.isPaused) {
                        holder.itemView.setOnClickListener {
                            GlobalScope.launch {
                                val intent = Intent(context, DownloadService::class.java)

                                intent.putExtra(DownloadService.ACTION_RESUME_DOWNLOAD, item)
                                ContextCompat.startForegroundService(context, intent)
                            }
                        }

                    } else {
                        GlobalScope.launch {
                            val intent = Intent(context, DownloadService::class.java)

                            intent.putExtra(DownloadService.EXTRA_DETAILS, item)
                            ContextCompat.startForegroundService(context, intent)
                        }
                        holder.itemView.setOnClickListener {
                            GlobalScope.launch {
                                val intent = Intent(context, DownloadService::class.java)

                                intent.putExtra(DownloadService.ACTION_PAUSE_DOWNLOAD, item)
                                ContextCompat.startForegroundService(context, intent)
                            }
                        }
                    }
                }
                }
            }
        } else {

            holder.tvStatus.text = "${item.fileSize} * ${item.downloadUrl}"
            holder.progressBar.visibility = View.GONE
            holder.tvProgress.visibility = View.GONE
            when (item.fileType) {
                "mp3" -> holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.music
                    )
                )

                "mp4" -> holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.video
                    )
                )

                "png" -> holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.photo
                    )
                )

                "jpg" -> holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.photo
                    )
                )

                else -> holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.file
                    )
                )
            }
            holder.itemView.setOnClickListener {
                try {

                    val mimeType = item.fileType?.let { it1 -> getMimeTypeFromExtension(it1) }
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setType(mimeType)
//                intent.setDataAndType(Uri.parse(item.fileUri), mimeType)
                    context.startActivity(intent)

                } catch (e: Exception) {
                    Toast.makeText(context, "No app to open", Toast.LENGTH_SHORT).show()
                }
            }

        }


        holder.ivDelete.setOnClickListener {
            deleteListener(item.downloadId)
        }

    }

    private fun getMimeTypeFromExtension(fileExtension: String): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
    }
}