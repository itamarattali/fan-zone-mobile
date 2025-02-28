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
        val scrollView = binding.dateSelectorScroll  // Reference to the HorizontalScrollView
        val calendar = Calendar.getInstance()

        // Start with 3 days before today, including today, and 3 days after
        calendar.add(Calendar.DAY_OF_YEAR, -3)  // Start 3 days before today

        var selectedButton: Button? = null  // Store reference to the currently selected button
        var todayButton: Button? = null  // Store reference to today's button
        var isTodayCentered = false  // Flag to check if today's button has been centered already

        // Create the buttons for the dates
        for (i in -3..3) {  // Show 3 days back, today, and 3 days forward
            val button = Button(requireContext()).apply {
                val date = calendar.time
                // Get the day of the week and the date
                val dayOfWeek = android.text.format.DateFormat.format("EEE", date).toString()
                val formattedDate = android.text.format.DateFormat.format("dd", date).toString()
                // Set the button text to display the date and day
                text = "$formattedDate\n$dayOfWeek"

                // Use the correct method to set the text appearance
                setTextAppearance(R.style.DateButtonStyle)

                // Handle button click
                setOnClickListener {
                    // Reset the previously selected button
                    selectedButton?.setBackgroundColor(resources.getColor(android.R.color.white)) // Reset background color

                    // Set the clicked button as selected
                    setBackgroundColor(resources.getColor(R.color.purple_500))  // Set the clicked button to purple background

                    // Update the reference to the selected button
                    selectedButton = this

                    // Scroll to the selected button to center it horizontally
                    scrollToCenter(this)

                    // Load matches for the selected date
                    matchListViewModel.loadMatchesForDate(date)
                }
            }

            // Add the button to the container
            dateContainer.addView(button)

            // Check if this is today's button
            if (calendar.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) &&
                calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                // Mark today's date as selected
                selectedButton = button
                todayButton = button
                button.setBackgroundColor(resources.getColor(R.color.purple_500))
            }

            calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
        }

        // Now, after the layout is fully set, we scroll to the center of the button for today
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
        // Get the width of the button and the HorizontalScrollView container
        val scrollViewWidth = binding.dateSelectorScroll.width
        val buttonWidth = button.width

        // Get the position of the button in the container (its left edge position)
        val buttonPosition = button.left

        // Calculate the offset required to center the button in the HorizontalScrollView
        val scrollTo = buttonPosition - (scrollViewWidth / 2) + (buttonWidth / 2)

        // Use smoothScrollTo to animate the scroll
        binding.dateSelectorScroll.smoothScrollTo(scrollTo, 0)
    }

}