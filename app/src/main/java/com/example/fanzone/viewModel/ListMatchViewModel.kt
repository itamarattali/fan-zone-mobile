package com.example.fanzone.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fanzone.apiCalls.FootballData
import com.example.fanzone.model.ListMatch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListMatchViewModel : ViewModel() {

    // LiveData to hold the list of matches
    private val _matches = MutableLiveData<MutableList<ListMatch>>(mutableListOf())
    val matches: LiveData<MutableList<ListMatch>> get() = _matches

    private val _filteredMatches = MutableLiveData<MutableList<ListMatch>>() // Filtered matches
    val filteredMatches: LiveData<MutableList<ListMatch>> get() = _filteredMatches

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun filterMatchesByDate(selectedDate: Date) {
        val selectedDateString = dateFormat.format(selectedDate)
        _filteredMatches.value = _matches.value?.filter { match ->
            dateFormat.format(match.date) == selectedDateString
        }?.toMutableList()
    }

    init {
        loadMatches()
    }

    private fun loadMatches() {
        FootballData.getMatches { fetchedMatches ->
            _matches.postValue(fetchedMatches ?: mutableListOf())
            Log.d("ListMatchViewModel", "Matches updated: $fetchedMatches")
        }
    }
}
