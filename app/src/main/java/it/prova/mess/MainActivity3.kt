package it.prova.mess

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.reflect.TypeToken
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList


class Messaggio{
    var messaggio: String? = null
    var id: String? = null
    var data: String? = null

    constructor(){}

    constructor(messaggio: String, Id: String){
        this.messaggio = messaggio
        this.id = Id
        this.data = Calendar.getInstance().time.toString()
    }
}

class MainActivity3 : AppCompatActivity() {
    private lateinit var messaggi: ArrayList<Messaggio>
    private lateinit var messaggioView: MesssaggiView
    private lateinit var chiave: String
    private lateinit var dbHelper : database
    private lateinit var db : SQLiteDatabase
    private lateinit var id_ricevente : String
    private lateinit var messaggi_salvati: ArrayList<Messaggio>
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar!!.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        messaggi = ArrayList()

        id_ricevente = intent!!.getStringExtra("firebase-id")!!
        val chat = findViewById<RecyclerView>(R.id.messaggi)
        val id = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val tasto_invio = findViewById<ImageView>(R.id.invio)
        val testo_t = findViewById<TextView>(R.id.scrivi)
        messaggioView = MesssaggiView(this, messaggi)
        val NomeView = findViewById<TextView>(R.id.nome)
        NomeView.setText(intent.getStringExtra("numero"))
        try {
            val foto: Bitmap = intent.getParcelableExtra("immagine")!!
            val immagineView = findViewById<ImageView>(R.id.imageView)
            immagineView.setImageBitmap(foto)
        } catch (e: Exception){
            println("nessuna immmagine")
        }
        val database = Firebase.database
        val myRef = database.getReferenceFromUrl("https://messaggi-f4a2d-default-rtdb.europe-west1.firebasedatabase.app/")
        Chiave(id, id_ricevente)


        dbHelper = database(this)
        db = dbHelper.writableDatabase
        chat.layoutManager = LinearLayoutManager(this)
        chat.adapter = messaggioView
        for (singolo in riprendichat(id_ricevente,db)) {
            messaggi.add(singolo)
        }
        messaggioView.notifyDataSetChanged()
        val indietro = findViewById<ImageView>(R.id.indietro)
        indietro.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            salvachat(messaggi, id_ricevente, db)
            startActivity(intent)
        }
        myRef.child("chats").child(id).child(id_ricevente).child("messaggi").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (nuovo in dataSnapshot.children){
                        val mess = nuovo.getValue(Messaggio::class.java)
                        if (messaggi.isNullOrEmpty()){
                            aggiungi(mess!!, id, id_ricevente)
                        } else if(mess!!.data!! > messaggi.last().data.toString()) {
                            aggiungi(mess, id, id_ricevente)
                        }
                }
                messaggioView.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                TODO("idk")
            }
        })


        tasto_invio.setOnClickListener() {
            val messaggio = Messaggio(testo_t.text.toString(), id)
            testo_t.text = ""
            if (messaggio.messaggio!!.trim().isNotEmpty()) {
                messaggio.messaggio = cifraMessaggioAES(messaggio.messaggio!!, Chiave(id,id_ricevente))
                if (id != id_ricevente) {
                    myRef.child("chats").child(id).child(id_ricevente).child("messaggi").push()
                        .setValue(messaggio).onSuccessTask {
                        myRef.child("chats").child(id_ricevente).child(id).child("messaggi").push()
                            .setValue(messaggio)
                    }
                } else {
                    myRef.child("chats").child(id).child(id_ricevente).child("messaggi").push()
                        .setValue(messaggio)
                }
            }
        }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Chiave(id: String, id_ricevente: String): String {
        if (this::chiave.isInitialized) {
            return chiave
        } else{
            val sharedpref = this.getSharedPreferences("chat_chiave", Context.MODE_PRIVATE)
            val chiavePref = sharedpref.getString("$id$id_ricevente", null)
            if (chiavePref == null) {
                runBlocking {
                    chiave = API_SERVER().chiave(id, id_ricevente, this@MainActivity3)
                }
                if (chiave != "Errore") {
                    val chiavecifrata = cifraRSA(chiave)
                    with(sharedpref.edit()) {
                        putString("$id$id_ricevente", chiavecifrata)
                        apply()
                    }
                } else {
                    Toast.makeText(this, "C'è stato un errore", Toast.LENGTH_LONG).show()
                }
            } else {
                chiave = decifraRSA(chiavePref)
            }
        }
        return chiave
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun salvachat(lista: ArrayList<Messaggio>, id: String, db: SQLiteDatabase){
        val gson = Gson()
        val json = gson.toJson(lista)
        val jsonCifrato = cifraMessaggioAES(json, chiave)
        dbHelper.inserisci(db,jsonCifrato!!,id, intent.getStringExtra("numero").toString())
        if (this::messaggi_salvati.isInitialized) {
            messaggi_salvati.addAll(messaggi)
        } else{
            messaggi_salvati = messaggi
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun riprendichat(id: String, db: SQLiteDatabase): ArrayList<Messaggio> {
        val gson = Gson()
        val json = dbHelper.prendi(db,id)
        if(!json.isNullOrEmpty()) {
            val type: Type = object : TypeToken<ArrayList<Messaggio?>?>() {}.type
            val decifrato = decifraMessaggioAES(json.toByteArray(), chiave)
            return (gson.fromJson<Any>(decifrato, type)
                ?: kotlin.collections.ArrayList<Messaggio>()) as ArrayList<Messaggio>
        } else{
            return ArrayList<Messaggio>()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun aggiungi(mess: Messaggio, id: String, id_ricevente: String){
        mess.messaggio = decifraMessaggioAES(
            mess.messaggio!!.toByteArray(Charsets.ISO_8859_1),
            Chiave(id, id_ricevente)
        )
        messaggi.add(mess)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        super.onStop()
        if(this::messaggi_salvati.isInitialized) {
            if (!messaggi_salvati.equals(messaggi)) {
                salvachat(messaggi, id_ricevente, db)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        if(this::messaggi_salvati.isInitialized) {
            if (!messaggi_salvati.equals(messaggi)) {
                salvachat(messaggi, id_ricevente, db)
            }
        }
    }

}

