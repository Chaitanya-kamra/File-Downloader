package com.chaitanya.filedownloader

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chaitanya.filedownloader.databinding.ItemDownloadBinding

class ItemAdapter( private val items: ArrayList<DownloadItem>,
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
        TODO("Not yet implemented")
    }
}