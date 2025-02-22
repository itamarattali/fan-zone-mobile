package com.example.fanzone

import ListMatchAdapter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.fanzone.viewModel.ListMatchViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MatchesFeedFragment : Fragment() {

    private val listMatchViewModel: ListMatchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_matches_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val matchesListView: ListView = view.findViewById(R.id.matches_list)

        // Observe LiveData and update the ListView
        listMatchViewModel.filteredMatches.observe(viewLifecycleOwner) { matchList  ->
            if (!matchList.isNullOrEmpty()){
                val adapter = ListMatchAdapter(requireContext(), matchList)
                matchesListView.adapter = adapter
            }else{
                val adapter = ListMatchAdapter(requireContext(), mutableListOf())
                matchesListView.adapter = adapter
            }
        }

        val horizontalScrollView = view.findViewById<HorizontalScrollView>(R.id.date_selector_scroll)
        val dateSelectorLinearLayout = horizontalScrollView.getChildAt(0) as LinearLayout

        val calendar = Calendar.getInstance()
        val today = SimpleDateFormat("dd-MM-yyyy").parse("01-02-2025")
        val dateFormat = SimpleDateFormat("dd\nEEE", Locale.getDefault())

        // Add dates: one week before and one week after
        for (i in -3..3) {
            val dateTextView = TextView(requireContext())
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, i)

            val date = calendar.time
            dateTextView.text = dateFormat.format(date)
            dateTextView.textSize = 20f
            dateTextView.setPadding(25, 10, 25, 10)
            dateTextView.gravity = android.view.Gravity.CENTER
            dateTextView.setBackgroundResource(
                if (i == 0) R.drawable.date_selector_selected else R.drawable.date_selector_unselected
            )
            dateTextView.setTextColor(Color.BLACK)

            // Add click listener to handle selection
            dateTextView.setOnClickListener {
                // Deselect all other TextViews
                for (j in 0 until dateSelectorLinearLayout.childCount) {
                    val child = dateSelectorLinearLayout.getChildAt(j)
                    child.setBackgroundResource(R.drawable.date_selector_unselected)
                }
                // Highlight the selected TextView
                dateTextView.setBackgroundResource(R.drawable.date_selector_selected)

                // Center the selected date in the HorizontalScrollView
                val scrollToX = dateTextView.left - (horizontalScrollView.width / 2) + (dateTextView.width / 2)
                horizontalScrollView.smoothScrollTo(scrollToX, 0)

                // Notify ViewModel to update matches for the selected date
                listMatchViewModel.filterMatchesByDate(date)
            }

            // Add the TextView to the LinearLayout
            dateSelectorLinearLayout.addView(dateTextView)

            // Automatically focus the scroll on the current date when the layout loads
            if (i == 0) {
                dateTextView.post {
                    val initialScrollToX = dateTextView.left - (horizontalScrollView.width / 2) + (dateTextView.width / 2)
                    horizontalScrollView.smoothScrollTo(initialScrollToX, 0)
                }

                // Filter matches for today's date
                listMatchViewModel.filterMatchesByDate(today)
            }
        }
    }
}
