package com.chaitanya.filedownloader.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chaitanya.filedownloader.databinding.ItemDateBinding
import com.chaitanya.filedownloader.models.DownloadEntity

class DateAdapter(
    private val items: ArrayList<DownloadEntity>,
private val deleteListener: (id: Int) -> Unit
) :
RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    class ViewHolder(binding: ItemDateBinding) : RecyclerView.ViewHolder(binding.root) {

        val tvTitle = binding.tvDate
        val rvCard = binding.rvDate

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.ViewHolder {
        return ItemAdapter.ViewHolder(
            ItemDateBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ItemAdapter.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}
