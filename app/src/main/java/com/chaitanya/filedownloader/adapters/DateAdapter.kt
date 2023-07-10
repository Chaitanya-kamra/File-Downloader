package com.chaitanya.filedownloader.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chaitanya.filedownloader.databinding.ItemDateBinding
import com.chaitanya.filedownloader.models.DownloadEntity

class DateAdapter(
    private val groupedEntities: Map<String?, ArrayList<DownloadEntity>>,
    private val deleteListener: (id: Int) -> Unit
) :
RecyclerView.Adapter<DateAdapter.ViewHolder>() {
    class ViewHolder(binding: ItemDateBinding) : RecyclerView.ViewHolder(binding.root) {

        val tvTitle = binding.tvDate
        val rvCard = binding.rvDate

    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dates = groupedEntities.keys.toList()
        val currentDate = dates[position]
        val entities = groupedEntities[currentDate]

        holder.tvTitle.text = currentDate

        // Create an instance of the inner RecyclerView adapter and set it to recyclerViewItems
        entities?.let {
            val innerAdapter = ItemAdapter(it,deleteListener)
            holder.rvCard.adapter = innerAdapter
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDateBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return groupedEntities.size
    }


}
