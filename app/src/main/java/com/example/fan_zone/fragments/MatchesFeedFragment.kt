package com.example.fan_zone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fan_zone.R
import com.example.fan_zone.adapters.MatchListAdapter
import com.example.fan_zone.databinding.FragmentMatchesFeedBinding
import com.example.fan_zone.viewModels.MatchListViewModel
import java.text.SimpleDateFormat
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
            if (!matches.isNullOrEmpty()){
                matchAdapter.updateMatches(matches)
            } else matchAdapter.updateMatches(mutableListOf())
        }

        setupDateSelector()
    }

    private fun setupDateSelector() {
        val dateContainer = binding.dateContainer
        val calendar = Calendar.getInstance()

        // Start with 3 days before today, including today, and 3 days after
        calendar.add(Calendar.DAY_OF_YEAR, -3)  // Start 3 days before today

        for (i in -3..3) {  // Show 3 days back, today, and 3 days forward
            val button = Button(requireContext()).apply {
                val date = calendar.time
                // Get the day of the week and the date
                val dayOfWeek = android.text.format.DateFormat.format("EEE", date).toString()
                val formattedDate = android.text.format.DateFormat.format("dd", date).toString()
                // Set the button text to display the date and day
                text = "$formattedDate\n$dayOfWeek"

                // Use the correct method to set the text appearance
                setTextAppearance(R.style.DateButtonStyle)  // No need for the context

                // Handle button click
                setOnClickListener {
                    matchListViewModel.loadMatchesForDate(date)
                }
            }

            // Add the button to the container
            dateContainer.addView(button)
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
        }
    }

}