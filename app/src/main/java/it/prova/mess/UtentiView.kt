package it.prova.mess

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.createBitmap
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.regex.Pattern

class UtentiView(val context: Context, val entry: ArrayList<Persone>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val lay = LayoutInflater.from(context).inflate(R.layout.contenitore, parent, false)
        return Seleziona(lay)
    }

    override fun getItemCount(): Int {
        return entry.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dati = entry[position]
        val viewHolder = holder as Seleziona
        holder.nome.text = dati.nome
        holder.immagine
        var bitmapScaled : Bitmap? = null
        var id : String? = null
        if (!dati.foto.isNullOrEmpty()) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            if (dati.nome == "io"){
                id = FirebaseAuth.getInstance().currentUser?.uid.toString()
            } else{
                id = dati.id!!
            }
            val imma = storageRef.child("immagini").child(id)
            imma.getBytes(10 * 1024 * 1024).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                bitmapScaled = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
                holder.immagine.setImageBitmap(bitmapScaled)
            }
        }
        holder.nome.setOnClickListener() {
            val intent = Intent(context, MainActivity3::class.java)
            intent.putExtra("firebase-id", id ?: dati.id)
            intent.putExtra("numero", dati.nome)
            intent.putExtra("immagine", bitmapScaled)
            context.startActivity(intent)
        }

    }

    class Seleziona(view: View) : RecyclerView.ViewHolder(view) {

        val nome = view.findViewById<TextView>(R.id.nome)

        val immagine = view.findViewById<ImageView>(R.id.imageView3)

    }

}