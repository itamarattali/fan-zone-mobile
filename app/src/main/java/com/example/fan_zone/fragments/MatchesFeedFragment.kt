package com.example.fan_zone.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fan_zone.R
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

        matchAdapter = MatchListAdapter(mutableListOf()) { match ->
            val action = MatchesFeedFragmentDirections.actionMatchesFeedFragmentToMatchDetailsFragment(matchId = match.id.toString())
            findNavController().navigate(action)
        }

        binding.matchesList.layoutManager = LinearLayoutManager(requireContext())
        binding.matchesList.adapter = matchAdapter

        matchListViewModel.matches.observe(viewLifecycleOwner) { matches ->
            if (!matches.isNullOrEmpty()) {
                matchAdapter.updateMatches(matches)
            } else {
                matchAdapter.updateMatches(mutableListOf())
            }
        }

        setupDateSelector()
    }

    @SuppressLint("ResourceAsColor")
    private fun setupDateSelector() {
        val dateContainer = binding.dateContainer
        val scrollView = binding.dateSelectorScroll
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_YEAR, -3)

        var selectedButton: Button? = null
        var todayButton: Button? = null
        var isTodayCentered = false

        for (i in -3..3) {
            val button = Button(requireContext()).apply {
                val date = calendar.time
                val dayOfWeek = android.text.format.DateFormat.format("EEE", date).toString()
                val formattedDate = android.text.format.DateFormat.format("dd", date).toString()
                text = "$formattedDate\n$dayOfWeek"
                setTextAppearance(R.style.DateButtonStyle)

                setOnClickListener {
                    selectedButton?.setTextAppearance(R.style.DateButtonStyle)
                    setTextAppearance(R.style.PickedDateButtonStyle)

                    selectedButton = this

                    scrollToCenter(this)
                    matchListViewModel.loadMatchesForDate(date)
                }
            }

            dateContainer.addView(button)

            if (calendar.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) &&
                calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                selectedButton = button
                todayButton = button
                button.setTextAppearance(R.style.PickedDateButtonStyle)
            }

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        scrollView.viewTreeObserver.addOnGlobalLayoutListener {
            todayButton?.let {
                if (!isTodayCentered) {
                    scrollToCenter(it)
                    isTodayCentered = true
                }
            }
        }
    }

    private fun scrollToCenter(button: Button) {
        val scrollViewWidth = binding.dateSelectorScroll.width
        val buttonWidth = button.width
        val buttonPosition = button.left

        val scrollTo = buttonPosition - (scrollViewWidth / 2) + (buttonWidth / 2)

        binding.dateSelectorScroll.smoothScrollTo(scrollTo, 0)
    }

}