package it.prova.mess

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.reflect.TypeToken
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.zetetic.database.sqlcipher.SQLiteConnection
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.reflect.Type
import java.net.Socket
import java.util.*
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
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
        Chiave(id, id_ricevente!!)


        dbHelper = database(this, PasswordDatabase(this))
        db = dbHelper.writableDatabase


        val indietro = findViewById<ImageView>(R.id.indietro)
        indietro.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            salvachat(messaggi, id_ricevente.toString(), db)
            startActivity(intent)
        }
        chat.layoutManager = LinearLayoutManager(this)
        chat.adapter = messaggioView
        myRef.child("chats").child(id).child(id_ricevente.toString()).child("messaggi").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (messaggi.isNullOrEmpty()) {
                    for (singolo in riprendichat(id_ricevente.toString(),db)) {
                        messaggi.add(singolo)
                    }
                }
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
                messaggio.messaggio = cifraMessaggioAES(messaggio.messaggio!!, Chiave(id,id_ricevente.toString())).toString()
                if (id != id_ricevente) {
                    myRef.child("chats").child(id).child(id_ricevente.toString()).child("messaggi").push()
                        .setValue(messaggio).onSuccessTask {
                        myRef.child("chats").child(id_ricevente.toString()).child(id).child("messaggi").push()
                            .setValue(messaggio)
                    }
                } else {
                    myRef.child("chats").child(id).child(id_ricevente.toString()).child("messaggi").push()
                        .setValue(messaggio)
                }
            }
        }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Chiave(id: String, id_ricevente: String): String {
        val sharedpref = this.getPreferences(Context.MODE_PRIVATE)
        var chiavePref = sharedpref.getString("$id$id_ricevente", null)
        if (chiavePref == null){
            runBlocking {
                chiave = Ottienichiave(id, id_ricevente).toString()
            }
            if(chiave != "errore") {
                val chiavecifrata = cifraRSA(chiave)
                with(sharedpref.edit()) {
                    putString("$id$id_ricevente", chiavecifrata)
                    apply()
                }}
        } else{
            chiave = decifraRSA(chiavePref)
        }
        return chiave
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun Ottienichiave(id: String, id_ricevente: String) : Any = withContext(
    Dispatchers.IO){
        var risultato = ""
        try {
            var connessione = SSLContext.getInstance("TLSv1.2")
            val keystore = Certificati(this@MainActivity3)
            val trustmanager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustmanager.init(keystore)
            connessione.init(null,trustmanager.trustManagers ,null)
            var socket = connessione.socketFactory
            var soc = socket.createSocket("192.168.178.22", 21402)
            val input = BufferedReader(InputStreamReader(soc.getInputStream()))
            val output = PrintWriter(soc.getOutputStream(), true)
            val dati = "chiave: $id $id_ricevente"
            val (cifrato, chiave) = cifraAES(dati)
            val ChiaveCifrata = RSA(chiave)
            output.println("$cifrato chiave: $ChiaveCifrata")
            while(input.ready()){
                delay(1)
            }
            risultato = input.readLine()
            //risultato = decifraAES(TestoRicevuto, chiave)
            soc.close()
        } catch (e : Exception){
            risultato = "errore"
        }

        return@withContext risultato
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun salvachat(lista: ArrayList<Messaggio>, id: String, db: SQLiteDatabase){
        val gson = Gson()
        val json = gson.toJson(lista)
        val jsonCifrato = cifraMessaggioAES(json, chiave)
        dbHelper.inserisci(db,jsonCifrato!!,id, intent.getStringExtra("numero").toString())
        messaggi_salvati.addAll(messaggi)
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

