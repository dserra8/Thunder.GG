package com.example.leagueapp1.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.leagueapp1.R
import com.example.leagueapp1.databinding.ChampItemBinding
import com.example.leagueapp1.feature_champions.util.ChampionConstants.CHAMP_SQUARE_SPLASH_URL
import java.text.NumberFormat
import java.util.*


class ChampionListAdapterNoHeader(private val onItemClicked: (ChampItem) -> Unit) :
    ListAdapter<ChampItem, ChampionListAdapterNoHeader.ChampionItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChampionItemViewHolder {

        val binding = ChampItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChampionItemViewHolder(binding, parent.context)
    }


    override fun onBindViewHolder(holder: ChampionItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, onItemClicked)
    }

    class ChampionItemViewHolder(
        private val binding: ChampItemBinding,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(champion: ChampItem, onItemClicked: (ChampItem) -> Unit) {

            itemView.setOnClickListener { onItemClicked(champion) }
            binding.apply {
                if (champion.champImageResource == 0) {
                    Glide.with(context)
                        .load("$CHAMP_SQUARE_SPLASH_URL${champion.formattedName}.png")
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(imageView)

                } else {
                    imageView.setImageResource(champion.champImageResource)
                }
                if(champion.isUpdate){
                    champItemRelativeLayout.setBackgroundResource(R.drawable.champ_update_indicator)
                } else{
                    champItemRelativeLayout.background = null
                }
                champName.text = champion.champName
                rankListImg.setImageResource(champion.rankImageResource)
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

