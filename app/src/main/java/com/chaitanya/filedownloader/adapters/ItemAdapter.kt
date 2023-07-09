package com.chaitanya.filedownloader.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chaitanya.filedownloader.R
import com.chaitanya.filedownloader.databinding.ItemDownloadBinding
import com.chaitanya.filedownloader.models.DownloadEntity

class ItemAdapter(private val items: ArrayList<DownloadEntity>,
                  private val deleteListener: (id: Int) -> Unit):
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
        holder.tvProgress.text = "${item.progress}%"
        holder.progressBar.progress = item.progress
        if (!item.isCompleted) {
            holder.ivIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.pause
                )
            )
            holder.tvStatus.text = "0 / ${item.fileSize}"
            if (item.fileStatus == "Queue") {
                holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.queue
                    )
                )
                holder.tvStatus.text = "Waiting  in queue"
            } else if (item.fileStatus == "Failed") {
                holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.error
                    )
                )
                holder.tvStatus.text = "Failed to download"
            } else if (item.isPaused) {
                holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.resume
                    )
                )
                holder.tvStatus.text = "Download paused"
            }
        }
    else{
            holder.tvStatus.text = "${item.fileSize} * ${item.downloadUrl}"
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
        }


        holder.ivDelete.setOnClickListener {
            deleteListener(item.downloadId)
        }
    }
}