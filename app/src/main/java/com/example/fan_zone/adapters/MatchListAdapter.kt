package com.example.fan_zone.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fan_zone.databinding.MatchRecyclerViewListItemBinding
import com.example.fan_zone.models.Match
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class MatchListAdapter(private val matches: MutableList<Match>, private val onItemClick: (Match) -> Unit) :
    RecyclerView.Adapter<MatchListAdapter.MatchViewHolder>() {

    class MatchViewHolder(private val binding: MatchRecyclerViewListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(match: Match, onItemClick: (Match) -> Unit) {
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:MM", Locale.getDefault())

            binding.title.text = "${match.homeTeam} vs ${match.awayTeam}"
            binding.kickOffTime.text = dateFormatter.format(match.date)
            Picasso.get().load(match.homeTeamImage ?: "").into(binding.homeTeamLogo)
            Picasso.get().load(match.awayTeamImage ?: "").into(binding.awayTeamLogo)
            binding.root.setOnClickListener {
                onItemClick(match)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = MatchRecyclerViewListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(matches[position], onItemClick)
    }

    override fun getItemCount() = matches.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateMatches(newMatches: List<Match>) {
        matches.clear()
        matches.addAll(newMatches)
        notifyDataSetChanged()
    }
}