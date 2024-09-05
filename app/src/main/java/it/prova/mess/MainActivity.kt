package it.prova.mess

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import it.prova.mess.R

class Persone{
    var nome : String? = null
     var id : String? = null
    var foto : String? = null
    constructor(){}
    constructor(NOME: String, ID: String, Foto: String){
        this.nome = NOME
        this.id = ID
        this.foto = Foto
    }
}

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener{
    private lateinit var listapersone: ArrayList<Persone>
    private lateinit var utentiview: UtentiView
    private lateinit var dbHelper: database
    private lateinit var db: SQLiteDatabase
    private lateinit var salvati: SharedPreferences
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var database_ref: DatabaseReference
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val server = API_SERVER()
        setContentView(R.layout.activity_main)
        runBlocking {
            var auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                val myIntent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(myIntent)
            }
        }
        dbHelper = database(this, PasswordDatabase(this))
        db = dbHelper.writableDatabase
        salvati = this.getPreferences(Context.MODE_PRIVATE)
        val database = Firebase.database
        database_ref = database.getReferenceFromUrl("https://messaggi-f4a2d-default-rtdb.europe-west1.firebasedatabase.app/")
        val numero = intent.getStringExtra("numero")
        val prefisso = intent.getStringExtra("prefisso")
        val password = intent.getStringExtra("password")
        if (salvati.getString("io", null) == null && FirebaseAuth.getInstance().currentUser?.uid != null) {
            val id = FirebaseAuth.getInstance().currentUser?.uid.toString()
            dbHelper.inserisci(db," ",id,numero.toString())
            salvati.edit().putString("io", numero).apply()
            runBlocking() {
                var risultato = server.reg_id(
                    prefisso.toString(),
                    numero.toString(),
                    id,
                    password!!,
                    this@MainActivity
                )
                if (risultato.trim() == "Successo") {
                    database_ref.child("chats").child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (nuovo_ID in snapshot.children) {
                                    if (nuovo_ID.key != "foto" && nuovo_ID.key != FirebaseAuth.getInstance().currentUser!!.uid) {
                                        var risultato = server.cerca_id(nuovo_ID.key!!)
                                        salvati.edit().putString(risultato, nuovo_ID.key!!).apply()
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                } else {
                    Toast.makeText(this@MainActivity, "Si è verificato un errore", Toast.LENGTH_LONG).show()
                    val myIntent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(myIntent)
                }
            }
        }
        Impostazioni()








        super.onCreate(savedInstanceState)
        listapersone = ArrayList()


        var firebaseid: String
        val Cerca = findViewById<SearchView>(R.id.Cerca)

        utentiview = UtentiView(this,listapersone)
        try {
        database_ref.child("chats").child(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (nuovo_ID in snapshot.children) {
                    if (nuovo_ID.key != "foto" && nuovo_ID.key != FirebaseAuth.getInstance().currentUser!!.uid) {
                        var risultato = server.cerca_id(nuovo_ID.key!!)
                        salvati.edit().putString(risultato, nuovo_ID.key!!)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        } catch (e: java.lang.Exception){
            println("errore: $e")
        }

        Cerca.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query!!.length == 10 && "^\\d+\$".toRegex().matches(query)){
                firebaseid = salvati.getString("$query","").toString()
                if (firebaseid.isNullOrEmpty()) {
                    firebaseid = server.cerca_numero(Cerca.query.toString())
                    if (firebaseid != "errore" && !firebaseid.isNullOrEmpty() && firebaseid != "Numero non trovato") {
                        with(salvati.edit()) {
                            putString("$query", firebaseid)
                            apply()
                        }
                        val intent = Intent(this@MainActivity, MainActivity3::class.java)
                        intent.putExtra("firebase-id", firebaseid)
                        intent.putExtra("numero", query)
                        startActivity(intent)
                        this@MainActivity.finish()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Si è verificato un errore",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                } else{
                    Toast.makeText(this@MainActivity, "Inserisci un numero valido", Toast.LENGTH_SHORT).show()
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })








        aggiornaView(dbHelper,db, database_ref)



    }

    private fun Impostazioni() {
        val impostazioni = findViewById<NavigationView>(R.id.impostazioni)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawerlayout);
        supportActionBar!!.hide()
        val toolbar: androidx.appcompat.widget.Toolbar  = this.findViewById(R.id.toolbar)
        val pulsante_att = ActionBarDrawerToggle(this,drawerLayout,toolbar ,R.string.app_name,R.string.app_name)
        drawerLayout.addDrawerListener(pulsante_att)
        pulsante_att.syncState()
        impostazioni.setNavigationItemSelectedListener(this)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        immagine_numero_profilo(impostazioni)
    }

    fun immagine_numero_profilo(navigationView: NavigationView){
        val header = navigationView.getHeaderView(0)
        val numView = header.findViewById<TextView>(R.id.numeroTel)
        numView.setText(salvati.getString("io", null))
        try {
            database_ref.child("chats").child(FirebaseAuth.getInstance().currentUser?.uid!!)
                .child("foto").get().addOnSuccessListener {
                if (it.value == "esiste") {
                    val immagineView = header.findViewById<ImageView>(R.id.immagineProfilo)
                    val storage = FirebaseStorage.getInstance()
                    val storageRef = storage.reference
                    val imma = storageRef.child("immagini")
                        .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    imma.getBytes(10 * 1024 * 1024).addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                        val bitmapPiccolo = Bitmap.createScaledBitmap(bitmap, 250, 250, true)
                        immagineView.setImageBitmap(bitmapPiccolo)
                    }
                }
            }
        } catch (e : Exception){
            println(e)
        }
    }

    private fun aggiornaView(
        dbHelper: database,
        db: SQLiteDatabase,
        database_ref: DatabaseReference
    ) {
        val persone = findViewById<RecyclerView>(R.id.persone)
        persone.layoutManager = LinearLayoutManager(this)
        persone.adapter = utentiview
        val tutti = salvati.all
        for (persona in tutti) {
            if (FirebaseAuth.getInstance().currentUser?.uid != null) {
                    var foto: String? = null
                    var id = " "
                    if (persona.key == "io") {
                        id = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    } else {
                        id = persona.value.toString()
                    }
                    database_ref.child("chats").child(id).child("foto")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                dbHelper.inseriscifoto(db, snapshot.value.toString(), id)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                dbHelper.inseriscifoto(db, "", id)
                            }

                        })
                    val uno =
                        Persone(persona.key, persona.value as String, dbHelper.prendifoto(db,id))
                    listapersone.add(uno)
                }
            }
        utentiview.notifyDataSetChanged()
    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.logout -> {
                Firebase.auth.signOut()
                val intent = Intent(this,LoginActivity::class.java)
                salvati.edit().clear().commit()
                dbHelper.cancella(db)
                startActivity(intent)
                this.deleteDatabase("database")
            }
            R.id.indietro ->{
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.cambiafoto -> {
                val intent = Intent(this, inserisci_foto::class.java)
                startActivity(intent)
            }


        }
        drawerLayout.closeDrawer(Gravity.LEFT)
        return true
    }

}




