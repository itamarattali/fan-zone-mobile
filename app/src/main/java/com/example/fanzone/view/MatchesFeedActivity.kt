package com.example.fanzone.view

import ListMatchAdapter
import ListMatchViewModel
import android.os.Bundle
import android.widget.ListView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.fanzone.R

class MatchesFeedActivity : AppCompatActivity() {
    private val listMatchViewModel: ListMatchViewModel by viewModels() // Using ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matches_feed)

        val listView: ListView = findViewById(R.id.matches_list)

        // Observe LiveData and update the ListView
        listMatchViewModel.matches.observe(this) { matches ->
            val adapter = ListMatchAdapter(this, matches)
            listView.adapter = adapter
        }
    }
}