package com.example.fan_zone.viewModels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.example.fan_zone.models.Match
import com.example.fan_zone.repositories.MatchRepository
import java.util.Calendar
import java.util.Date

class MatchListViewModel(application: Application) : AndroidViewModel(application) {

    private val matchRepository = MatchRepository(application)

    private val _selectedDate = MutableLiveData<Date>()

    val matches: LiveData<List<Match>> get() = _selectedDate.switchMap { date: Date -> matchRepository.getMatchesByDate(date) }

    init {
        val today = Calendar.getInstance().time
        _selectedDate.value = today
    }

    fun loadMatchesForDate(date: Date) {
        _selectedDate.value = date
    }

}
