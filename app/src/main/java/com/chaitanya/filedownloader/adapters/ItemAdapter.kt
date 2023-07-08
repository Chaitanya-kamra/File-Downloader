package com.chaitanya.filedownloader.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chaitanya.filedownloader.databinding.ItemDownloadBinding
import com.chaitanya.filedownloader.models.DownloadEntity

class ItemAdapter(private val items: ArrayList<DownloadEntity>,
                  private val updateListener: (id: Int) -> Unit):
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

    }
}