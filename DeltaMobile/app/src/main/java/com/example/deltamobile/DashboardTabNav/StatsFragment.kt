package com.example.deltamobile.DashboardTabNav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.deltamobile.Dashboard
import com.example.deltamobile.MainActivity
import com.example.deltamobile.R
import kotlinx.android.synthetic.main.dashboard__frag_stats.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException
import kotlin.system.exitProcess

class StatsFragment : Fragment() {
    private val baseUrl = "https://www.merriam-webster.com/dictionary/"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // see coroutines in fragments here:
        // https://stackoverflow.com/questions/59608923/launch-coroutine-from-click-event-in-fragment
        val job = Job()
        val uiScope = CoroutineScope(Dispatchers.Main + job)

        btnClicker.setOnClickListener{
            // get edit text input
            //
            val query = etInputData.text.toString();
            Toast.makeText(this.context,"Searching $query...",Toast.LENGTH_SHORT).show()


            // start coroutine
            uiScope.launch(Dispatchers.Default){
                // async op
                val strbldRes:StringBuilder = sendWordToWebster(query)
                withContext(Dispatchers.Main){
                    // ui op
                    tvDictionaryEntry.text = strbldRes.toString()
                }
            }

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dashboard__frag_stats, container, false)
    }

    // async func
    private suspend fun sendWordToWebster(word:String):StringBuilder{
        val url = this.baseUrl + word + "/"
        val strBuilder = StringBuilder()

        try{
            val doc:org.jsoup.nodes.Document = Jsoup.connect(url).get()
            // get definitions
            //
            val classMeanings: Elements = doc.select(".dtText")
            for(p in classMeanings){
                strBuilder.append(p.text()).append("\n")
            }
        }catch(e:IOException){
            println("io exception error");
            exitProcess(1)
        }

        return strBuilder
    }
}