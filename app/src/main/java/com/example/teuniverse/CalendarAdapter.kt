package com.example.teuniverse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class CalendarAdapter(private val itemList: ArrayList<Event>): RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_rv_item, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.type.setImageResource(currentItem.type.toInt())
        holder.content.text = currentItem.content
        holder.startAt.text = currentItem.startAt
    }

    override fun getItemCount(): Int {
        return itemList.count()
    }

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val type: ImageView = itemView.findViewById(R.id.event_icon)
        val content: TextView = itemView.findViewById(R.id.schedule)
        val startAt: TextView = itemView.findViewById(R.id.time)
    }
}