package com.crowdsensing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crowdsensing.model.Session

class SessionAdapter(
    private val sessions: MutableList<Session>,
    private val onItemClick: (Session) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    fun updateData(newSessions: List<Session>) {
        sessions.clear()
        sessions.addAll(newSessions)
        notifyDataSetChanged()
    }

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.date)
        val phoneModel: TextView = itemView.findViewById(R.id.phoneModel)
        val comment: TextView = itemView.findViewById(R.id.comment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.date.text = session.startTime.toString() ?: "Empty"
        holder.phoneModel.text = session.phoneModel?: "Unknown"
        holder.comment.text = session.description?.ifBlank { "No comment" }

        holder.itemView.setOnClickListener { onItemClick(session) }
    }

    override fun getItemCount(): Int = sessions.size
}

