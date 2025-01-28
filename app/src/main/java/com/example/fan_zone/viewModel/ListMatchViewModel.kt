import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fan_zone.R
import com.example.fan_zone.ListMatch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class ListMatchViewModel : ViewModel() {

    // LiveData to hold the list of matches
    private val _matches = MutableLiveData<List<ListMatch>>()
    val matches: LiveData<List<ListMatch>> get() = _matches

    private val _filteredMatches = MutableLiveData<List<ListMatch>>() // Filtered matches
    val filteredMatches: LiveData<List<ListMatch>> get() = _filteredMatches

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun filterMatchesByDate(selectedDate: Date) {
        val selectedDateString = dateFormat.format(selectedDate)
        _filteredMatches.value = _matches.value?.filter { match ->
            dateFormat.format(match.date) == selectedDateString
        }
    }

    init {
        // Initialize with sample data
        loadMatches()
    }

    private fun loadMatches() {
        val sampleMatches = listOf(
            ListMatch("Man Utd vs Chelsea", getRandomDay(),"15:00", R.drawable.logo_manutd),
            ListMatch("Arsenal vs Liverpool", getRandomDay(), "17:30", R.drawable.logo_arsenal),
            ListMatch("Man City vs Tottenham", getRandomDay(), "18:00", R.drawable.logo_3),
            ListMatch("Leicester vs Everton", getRandomDay(), "20:00", R.drawable.logo_4),
            ListMatch("West Ham vs Wolves", getRandomDay(), "21:00", R.drawable.logo_westham),
            ListMatch("Man Utd vs Chelsea", getRandomDay(),"15:00", R.drawable.logo_manutd),
            ListMatch("Arsenal vs Liverpool", getRandomDay(), "17:30", R.drawable.logo_arsenal),
            ListMatch("Man City vs Tottenham", getRandomDay(), "18:00", R.drawable.logo_3),
            ListMatch("Leicester vs Everton", getRandomDay(), "20:00", R.drawable.logo_4),
            ListMatch("West Ham vs Wolves", getRandomDay(), "21:00", R.drawable.logo_westham),
            ListMatch("Man Utd vs Chelsea", getRandomDay(),"15:00", R.drawable.logo_manutd),
            ListMatch("Arsenal vs Liverpool", getRandomDay(), "17:30", R.drawable.logo_arsenal),
            ListMatch("Man City vs Tottenham", getRandomDay(), "18:00", R.drawable.logo_3),
            ListMatch("Leicester vs Everton", getRandomDay(), "20:00", R.drawable.logo_4),
            ListMatch("West Ham vs Wolves", getRandomDay(), "21:00", R.drawable.logo_westham),
            ListMatch("Man Utd vs Chelsea", getRandomDay(),"15:00", R.drawable.logo_manutd),
            ListMatch("Arsenal vs Liverpool", getRandomDay(), "17:30", R.drawable.logo_arsenal),
            ListMatch("Man City vs Tottenham", getRandomDay(), "18:00", R.drawable.logo_3),
            ListMatch("Leicester vs Everton", getRandomDay(), "20:00", R.drawable.logo_4),
            ListMatch("West Ham vs Wolves", getRandomDay(), "21:00", R.drawable.logo_westham),
        )
        _matches.value = sampleMatches
    }

    fun getRandomDay(): Date {
        val calendar = Calendar.getInstance()
        val randomOffset = Random.nextInt(-4, 5) // Range: -4 to +4
        calendar.add(Calendar.DAY_OF_YEAR, randomOffset) // Adjust the date
        return calendar.time
    }
}
