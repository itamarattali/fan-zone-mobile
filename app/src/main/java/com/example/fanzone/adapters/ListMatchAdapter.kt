import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.fanzone.R
import com.example.fanzone.model.ListMatch

class ListMatchAdapter(context: Context, matches: List<ListMatch>) :
    ArrayAdapter<ListMatch>(context, 0, matches) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_match, parent, false)

        val match = getItem(position)
        val textViewMatchName = view.findViewById<TextView>(R.id.textViewMatchName)
        val textViewKickoff = view.findViewById<TextView>(R.id.textViewKickoff)
        val imageViewTeamLogo = view.findViewById<ImageView>(R.id.imageViewTeamLogo)

        textViewMatchName.text = match?.name
        textViewKickoff.text = "Kickoff: ${match?.kickoffTime}"
        imageViewTeamLogo.setImageResource(match?.logoResId ?: 0)

        return view
    }
}
