package com.example.weatherapp.ui.adapter

import android.content.Context
import android.view.*
import android.widget.*
import com.example.weatherapp.R

class LocationAdapter(
    context: Context,
    private val items: MutableList<String>,
    private val onDelete: (String) -> Unit,
    private val onClick: (String) -> Unit
) : ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_location, parent, false)

        val cityText = view.findViewById<TextView>(R.id.cityNameTextView)
        val deleteBtn = view.findViewById<ImageButton>(R.id.deleteButton)

        val city = items[position]
        cityText.text = city

        deleteBtn.setOnClickListener {
            onDelete(city)
        }

        view.setOnClickListener {
            onClick(city)
        }

        return view
    }

    fun updateData(newList: List<String>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
