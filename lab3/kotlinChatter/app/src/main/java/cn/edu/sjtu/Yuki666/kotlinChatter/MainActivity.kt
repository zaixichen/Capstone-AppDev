package cn.edu.sjtu.Yuki666.kotlinChatter

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import cn.edu.sjtu.Yuki666.kotlinChatter.ChattStore.chatts
import cn.edu.sjtu.Yuki666.kotlinChatter.ChattStore.getChatts
import cn.edu.sjtu.Yuki666.kotlinChatter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var view: ActivityMainBinding
    private lateinit var chattListAdapter: ChattListAdapter

    private val propertyObserver = object : ObservableList.OnListChangedCallback<ObservableArrayList<Int>>() {
        override fun onChanged(sender: ObservableArrayList<Int>?) { }

        override fun onItemRangeChanged(sender: ObservableArrayList<Int>?, positionStart: Int, itemCount: Int) { }

        override fun onItemRangeInserted(
            sender: ObservableArrayList<Int>?,
            positionStart: Int,
            itemCount: Int
        ) {
            println("onItemRangeInserted: $positionStart, $itemCount")
            runOnUiThread {
                chattListAdapter.notifyDataSetChanged()
            }
        }

        override fun onItemRangeMoved(
            sender: ObservableArrayList<Int>?,
            fromPosition: Int,
            toPosition: Int,
            itemCount: Int
        ) { }

        override fun onItemRangeRemoved(sender: ObservableArrayList<Int>?, positionStart: Int, itemCount: Int) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)

        chattListAdapter = ChattListAdapter(this, chatts)
        view.listView.adapter = chattListAdapter

        view.refreshContainer.setOnRefreshListener { refreshTimeline() }

        chatts.addOnListChangedCallback(propertyObserver)

        getChatts()
    }

    override fun onDestroy() {
        super.onDestroy()
        chatts.removeOnListChangedCallback(propertyObserver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_post) {
            startActivity(Intent(this, PostActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshTimeline() {
        getChatts()
        view.refreshContainer.isRefreshing = false
    }
}
