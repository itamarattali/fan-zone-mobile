package com.example.fan_zone.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fan_zone.models.Match
import com.example.fan_zone.repositories.MatchRepository
import kotlinx.coroutines.launch
import java.util.*

class MatchListViewModel : ViewModel() {
    private val matchRepository = MatchRepository()

    private val _matches = MutableLiveData<List<Match>>()
    val matches: LiveData<List<Match>> get() = _matches

    fun fetchMatchesForDate(date: Date) {
        viewModelScope.launch {
            val matchList = matchRepository.getMatchesByDate(date)
            _matches.value = matchList
        }
    }
}
