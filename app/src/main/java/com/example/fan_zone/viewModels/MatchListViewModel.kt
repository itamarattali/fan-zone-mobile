package com.example.fan_zone.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fan_zone.models.Match
import com.example.fan_zone.repositories.MatchRepository
import java.util.*

class MatchListViewModel : ViewModel() {
    private val matchRepository = MatchRepository()
    private val _matches = MutableLiveData<List<Match>>()
    val matches: LiveData<List<Match>> get() = _matches

    private val _selectedDate = MutableLiveData<Date>()
    val selectedDate: LiveData<Date> get() = _selectedDate

    init {
        val today = Calendar.getInstance().time
        _selectedDate.value = today
        loadMatchesForDate(today)
    }

    fun loadMatchesForDate(date: Date) {
        matchRepository.getMatchesByDate(selectedDate.value).removeObserver {}

        _selectedDate.value = date
        matchRepository.getMatchesByDate(date).observeForever {
            _matches.value = it
        }
    }

    override fun onCleared() {
        super.onCleared()
        matchRepository.getMatchesByDate(selectedDate.value).removeObserver { }
    }
}

