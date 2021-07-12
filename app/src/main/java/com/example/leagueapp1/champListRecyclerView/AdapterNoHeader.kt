package com.example.leagueapp1.champListRecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.leagueapp1.databinding.ChampItemBinding
import java.text.NumberFormat
import java.util.*


class ChampionListAdapterNoHeader(private val onItemClicked: (ChampItem) -> Unit) :
    ListAdapter<ChampItem, ChampionListAdapterNoHeader.ChampionItemViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChampionItemViewHolder {

        val binding = ChampItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChampionItemViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ChampionItemViewHolder, position: Int) {

        val currentItem = getItem(position)
        holder.bind(currentItem, onItemClicked)
    }

    class ChampionItemViewHolder(private val binding: ChampItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(champion: ChampItem, onItemClicked: (ChampItem) -> Unit) {

            itemView.setOnClickListener { onItemClicked(champion) }
            binding.apply {
                champName.text = champion.champName
                imageView.setImageResource(champion.imageResource)
                masteryPoints.text =
                    NumberFormat.getNumberInstance(Locale.US).format(champion.masteryPoints)

            }
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<ChampItem>() {
        override fun areItemsTheSame(oldItem: ChampItem, newItem: ChampItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChampItem, newItem: ChampItem) =
            oldItem == newItem
    }

}

