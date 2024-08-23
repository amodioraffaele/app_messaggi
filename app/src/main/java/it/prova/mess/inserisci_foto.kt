package it.prova.mess

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage


class inserisci_foto : AppCompatActivity() {
    val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        val URI_SELEZIONATA = it
        try{
           // immagine.setImageURI(URI_SELEZIONATA)
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference

            storageRef.child("immagini").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).putFile(URI_SELEZIONATA!!).addOnSuccessListener {
                val database = Firebase.database
                val database_ref = database.getReferenceFromUrl("https://messaggi-f4a2d-default-rtdb.europe-west1.firebasedatabase.app/")
                database_ref.child("chats").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("foto").setValue("esiste")
            }

        }catch(e:Exception){
            e.printStackTrace()
        }
        val intent = Intent(this@inserisci_foto, MainActivity::class.java)
        startActivity(intent)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_inserisci_foto)
        val testo = findViewById<TextView>(R.id.testo_aggiungi)
        testo.setOnClickListener(){
            galleryLauncher.launch("image/*")
        }
    }

}