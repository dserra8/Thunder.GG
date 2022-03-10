package com.example.leagueapp1.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.leagueapp1.databinding.ChampItemBinding
import com.example.leagueapp1.databinding.SummonerHeaderBinding
import com.example.leagueapp1.core.util.Constants.PROFILE_ICON_URL
import com.example.leagueapp1.core.util.Constants.SPLASH_ART_URL
import com.example.leagueapp1.core.util.exhaustive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ClassCastException
import java.text.NumberFormat
import java.util.*

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class ChampionListAdapterWithHeader(private val onItemClicked: (ChampItem) ->Unit) : ListAdapter<ChampionItem, RecyclerView.ViewHolder>(DiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when(viewType){
            ITEM_VIEW_TYPE_HEADER ->{
                val binding = SummonerHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SummonerHeaderViewHolder(binding)
            }
            ITEM_VIEW_TYPE_ITEM -> {
                val binding = ChampItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ChampionItemViewHolder(binding)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChampionItem.ChampInfo -> ITEM_VIEW_TYPE_ITEM
            is ChampionItem.Header -> ITEM_VIEW_TYPE_HEADER
        }.exhaustive
    }

    fun addHeaderAndSubmitList(list: List<ChampItem>?, header: HeaderItem){
        adapterScope.launch {
            val items = when(list){
                null -> listOf(ChampionItem.Header(header))
                else -> listOf(ChampionItem.Header(header)) + list.map{ChampionItem.ChampInfo(it)}
            }
            withContext(Dispatchers.Main){
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is ChampionItemViewHolder -> {
                val currentItem = getItem(position) as ChampionItem.ChampInfo
                holder.bind(currentItem.champ, onItemClicked)
            }
            is SummonerHeaderViewHolder -> {
                val currentItem = getItem(position) as ChampionItem.Header

                holder.bind(currentItem.header)
            }
        }
    }

    class ChampionItemViewHolder(private val binding: ChampItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(champion: ChampItem, onItemClicked: (ChampItem) -> Unit){

            itemView.setOnClickListener { onItemClicked(champion) }
            binding.apply {
                champName.text = champion.champName
                imageView.setImageResource(champion.champImageResource)
                masteryPoints.text = NumberFormat.getNumberInstance(Locale.US).format(champion.masteryPoints)

            }
        }
    }

    class SummonerHeaderViewHolder(private val binding: SummonerHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: HeaderItem) {
            binding.apply {
                summonerName.text = header.name
                val profileIconUrl = "$PROFILE_ICON_URL${header.summonerIconId}.png"
                val splashArtUrl = "$SPLASH_ART_URL${header.splashName}_0.jpg"
                Glide.with(summonerIcon)
                    .load(profileIconUrl)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(summonerIcon)
                Glide.with(splashArt)
                    .load(splashArtUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(splashArt)
            }
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<ChampionItem>() {
        override fun areItemsTheSame(oldItem: ChampionItem, newItem: ChampionItem) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChampionItem, newItem: ChampionItem) =
            oldItem == newItem
    }

}

sealed class ChampionItem{
    data class ChampInfo(val champ: ChampItem): ChampionItem(){
        override val id = champ.id
    }
    data class Header(val header: HeaderItem): ChampionItem(){
        override val id = Int.MIN_VALUE
    }
    abstract val id: Int
}
