package com.example.teuniverse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(private val itemList: ArrayList<CalendarItem>): RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_rv_item, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.eventIcon.setImageResource(currentItem.eventIcon)
        holder.time.text = currentItem.time
        holder.schedule.text = currentItem.schedule
    }

    override fun getItemCount(): Int {
        return itemList.count()
    }

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventIcon: ImageView = itemView.findViewById(R.id.event_icon)
        val time: TextView = itemView.findViewById(R.id.time)
        val schedule: TextView = itemView.findViewById(R.id.schedule)
    }
}