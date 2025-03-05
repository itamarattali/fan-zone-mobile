package com.example.fan_zone.viewModels


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fan_zone.models.Match
import com.example.fan_zone.repositories.MatchRepository
import java.util.Calendar
import java.util.Date

class MatchListViewModel(application: Application) : AndroidViewModel(application) {

    private val matchRepository = MatchRepository(application)
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
        _selectedDate.value = date
        selectedDate.value?.let {
            matchRepository.getMatchesByDate(it).observeForever {
                _matches.value = it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        selectedDate.value?.let { matchRepository.getMatchesByDate(it).removeObserver { } }
    }
}
