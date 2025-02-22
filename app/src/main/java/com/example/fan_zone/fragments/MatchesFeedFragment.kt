package com.example.fan_zone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fan_zone.adapters.MatchListAdapter
import com.example.fan_zone.databinding.FragmentMatchesFeedBinding
import com.example.fan_zone.viewModels.MatchListViewModel
import java.util.*

class MatchesFeedFragment : Fragment() {
    private lateinit var binding: FragmentMatchesFeedBinding
    private val matchListViewModel: MatchListViewModel by viewModels()
    private lateinit var matchAdapter: MatchListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMatchesFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        matchAdapter = MatchListAdapter(mutableListOf())
        binding.matchesList.layoutManager = LinearLayoutManager(requireContext())
        binding.matchesList.adapter = matchAdapter

        matchListViewModel.matches.observe(viewLifecycleOwner) { matches ->
            matchAdapter.updateMatches(matches)
        }

        setupDateSelector()
    }

    private fun setupDateSelector() {
        val dateContainer = binding.dateContainer
        val calendar = Calendar.getInstance()

        for (i in -3..3) {  // Show 3 days back and 3 days forward
            val button = Button(requireContext()).apply {
                val date = calendar.time

                setOnClickListener {
                    matchListViewModel.loadMatchesForDate(date)
                }
            }

            dateContainer.addView(button)
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to next day
        }
    }
}