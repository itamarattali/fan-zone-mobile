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
import com.example.fan_zone.viewModel.MatchListViewModel
import java.text.SimpleDateFormat
import java.util.*

class MatchesFeedFragment : Fragment() {

    private var _binding: FragmentMatchesFeedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MatchListViewModel by viewModels()

    private lateinit var matchListAdapter: MatchListAdapter
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchesFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        matchListAdapter = MatchListAdapter(emptyList())
        binding.matchesList.layoutManager = LinearLayoutManager(requireContext())
        binding.matchesList.adapter = matchListAdapter

        setupDateSelector()

        // Load matches for today's date by default
        val today = Calendar.getInstance().time
        viewModel.fetchMatchesForDate(today)

        viewModel.matches.observe(viewLifecycleOwner) { matches ->
            matchListAdapter = MatchListAdapter(matches)
            binding.matchesList.adapter = matchListAdapter
        }
    }

    private fun setupDateSelector() {
        val dateContainer = binding.dateContainer
        val calendar = Calendar.getInstance()

        for (i in 0..6) {  // Show next 7 days
            val button = Button(requireContext()).apply {
                val date = calendar.time
                text = dateFormatter.format(date)
                setOnClickListener {
                    viewModel.fetchMatchesForDate(date)
                }
            }
            dateContainer.addView(button)
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to next day
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
