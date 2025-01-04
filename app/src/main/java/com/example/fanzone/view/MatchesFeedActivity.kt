package com.example.fanzone.view

import ListMatchAdapter
import ListMatchViewModel
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ListView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.fanzone.R
import android.graphics.Color
import android.widget.HorizontalScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MatchesFeedActivity : AppCompatActivity() {
    private val listMatchViewModel: ListMatchViewModel by viewModels() // Using ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matches_feed)

        val matchesListView: ListView = findViewById(R.id.matches_list)

        // Observe LiveData and update the ListView
        listMatchViewModel.filteredMatches.observe(this) { matches ->
            val adapter = ListMatchAdapter(this, matches)
            matchesListView.adapter = adapter
        }

        val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.date_selector_scroll)
        val dateSelectorLinearLayout = horizontalScrollView.getChildAt(0) as LinearLayout

        val calendar = Calendar.getInstance()
        val today = calendar.time
        val dateFormat = SimpleDateFormat("dd\nEEE", Locale.getDefault())

        // Add dates: one week before and one week after
        for (i in -4..4) {
            val dateTextView = TextView(this)
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, i)

            val date = calendar.time
            dateTextView.text = dateFormat.format(date)
            dateTextView.textSize = 14f
            dateTextView.setPadding(16, 8, 16, 8)
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