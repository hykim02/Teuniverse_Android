package com.example.teuniverse

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SelectArtistAdapter(private val context: Context, private val artistList: List<FavoriteArtist>) : BaseAdapter() {

    override fun getCount(): Int {
        return artistList.size
    }

    override fun getItem(position: Int): Any = artistList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.grid_layout_item, null)

        val imageView: ImageView = view.findViewById(R.id.artist_image)
        val textView: TextView = view.findViewById(R.id.artist_name)

        val item = artistList[position]

        Glide.with(context)
            .load(item.imgUrl)
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(imageView)

        textView.text = item.name

        return view
    }
}
