package cn.edu.sjtu.Yuki666.kotlinChatter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import coil.load

class ChattListAdapter(context: Context, chatts: List<Chatt>) :
        ArrayAdapter<Chatt>(context, 0, chatts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.listitem_chatt, parent, false)

        getItem(position)?.run {
            listItemView.findViewById<TextView>(R.id.usernameTextView).text = username
            listItemView.findViewById<TextView>(R.id.messageTextView).text = message
            listItemView.findViewById<TextView>(R.id.timestampTextView).text = timestamp

            imageUrl?.let {
                listItemView.findViewById<ImageView>(R.id.chattImage).visibility = View.VISIBLE
                listItemView.findViewById<ImageView>(R.id.chattImage).load(it) {
                    crossfade(true)
                    crossfade(1000)
                }
            } ?: run {
                listItemView.findViewById<ImageView>(R.id.chattImage).visibility = View.GONE
                listItemView.findViewById<ImageView>(R.id.chattImage).setImageBitmap(null)
            }

            videoUrl?.let {
                listItemView.findViewById<ImageButton>(R.id.videoButton).visibility = View.VISIBLE
                listItemView.findViewById<ImageButton>(R.id.videoButton).setOnClickListener { v: View ->
                    if (v.id == R.id.videoButton) {
                        val intent = Intent(context, VideoPlayActivity::class.java)
                        intent.putExtra("VIDEO_URI", Uri.parse(it))
                        context.startActivity(intent)
                    }
                }
            } ?: run {
                listItemView.findViewById<ImageButton>(R.id.videoButton).visibility = View.INVISIBLE
                listItemView.findViewById<ImageButton>(R.id.videoButton).setOnClickListener(null)
            }
        }

        return listItemView
    }
}
