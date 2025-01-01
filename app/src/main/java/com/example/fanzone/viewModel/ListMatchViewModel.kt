import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fanzone.R
import com.example.fanzone.model.ListMatch

class ListMatchViewModel : ViewModel() {

    // LiveData to hold the list of matches
    private val _matches = MutableLiveData<List<ListMatch>>()
    val matches: LiveData<List<ListMatch>> get() = _matches

    init {
        // Initialize with sample data
        loadMatches()
    }

    private fun loadMatches() {
        val sampleMatches = listOf(
            ListMatch("Man Utd vs Chelsea", "15:00", R.drawable.logo_manutd),
            ListMatch("Arsenal vs Liverpool", "17:30", R.drawable.logo_arsenal),
            ListMatch("Man City vs Tottenham", "18:00", R.drawable.logo_3),
            ListMatch("Leicester vs Everton", "20:00", R.drawable.logo_4),
            ListMatch("West Ham vs Wolves", "21:00", R.drawable.logo_westham)
        )
        _matches.value = sampleMatches
    }
}
