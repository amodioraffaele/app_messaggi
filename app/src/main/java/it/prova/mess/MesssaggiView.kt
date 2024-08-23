package it.prova.mess

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MesssaggiView(val context: Context, val messaggi: ArrayList<Messaggio>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val inviato = 1
    val ricevuto = 2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == inviato){
            val view = LayoutInflater.from(context).inflate(R.layout.inviato, parent, false)
            return Inviato(view)
        } else{
            val view = LayoutInflater.from(context).inflate(R.layout.ricevuto, parent, false)
            return Ricevuto(view)
        }
    }

    override fun getItemCount(): Int {
        return messaggi.size
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messaggio = messaggi[position]
        if (holder.javaClass == Inviato::class.java) {
            val viewHolder = holder as Inviato
            holder.messaggioInviato.text = messaggio.messaggio
            holder.OrarioInviato.text = messaggio.data
        } else {
            val viewHolder = holder as Ricevuto
            holder.messaggioRicevuto.text = messaggio.messaggio
            holder.OrarioRicevuto.text = messaggio.data
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messaggio = messaggi[position]
        if (messaggio.id == FirebaseAuth.getInstance().currentUser?.uid){
            return inviato
        } else{
            return ricevuto
        }
    }
    class Inviato(view : View): RecyclerView.ViewHolder(view){
        val messaggioInviato = view.findViewById<TextView>(R.id.inviato)
        val OrarioInviato = view.findViewById<TextView>(R.id.orarioInviato)

    }
    class Ricevuto(view: View): RecyclerView.ViewHolder(view){
        val messaggioRicevuto = view.findViewById<TextView>(R.id.ricevuto)
        val OrarioRicevuto = view.findViewById<TextView>(R.id.orario)
    }
}