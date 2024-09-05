package it.prova.mess

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.mindrot.jbcrypt.BCrypt
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class Registrazione : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_registrazione)
        val PrefissoView = findViewById<Spinner>(R.id.scelta)
        PrefissoView.adapter = ArrayAdapter.createFromResource(this, R.array.prefissi, R.layout.spinnerlayout)
        val numeroView = findViewById<EditText>(R.id.Numero)
        val passwordView = findViewById<EditText>(R.id.password)
        val RipetiView = findViewById<EditText>(R.id.password1)
        var registrazione = findViewById<Button>(R.id.Registrati)
        val contatta = API_SERVER()
        registrazione.setOnClickListener(){
            val salt = BCrypt.gensalt()
            val numero = numeroView.text.toString()
            var password = passwordView.text.toString()
            val prefissoScelto = PrefissoView.selectedItem
            if (numero.length == 10 && "^\\d+\$".toRegex().matches(numero)) {
                if (password == RipetiView.text.toString()) {
                    if ((password.contains("[A-Za-z0-9!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())) && password.length > 10) {
                        password = password.hash(salt)
                        var risultato = ""
                        runBlocking {
                            risultato = contatta.registrazione(
                                prefissoScelto.toString(),
                                numero,
                                password,
                            )
                        }
                        numeroView.setText("")
                        passwordView.setText("")
                        if (risultato == "Successo") {
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.putExtra("numero", numero)
                            intent.putExtra("prefisso", PrefissoView.selectedItemPosition)
                            startActivity(intent)
                        } else{
                            val testo = findViewById<TextView>(R.id.textView3)
                            testo.setText(risultato)
                        }
                    } else {
                        val testo = findViewById<TextView>(R.id.textView3)
                        testo.setText("La password deve contenere almeno 10 caratteri tra cui:\nUna lettera maiuscola\n" +
                                "Una lettera minuscola\nUn numero\nUn carattere speciale")
                        testo.visibility = View.VISIBLE
                    }
                }else{
                    val testo = findViewById<TextView>(R.id.textView3)
                    testo.setText("Le password non corrispondono")
                    testo.visibility = View.VISIBLE
                }
            } else{
                Toast.makeText(this, "Il numero deve essere di 10 cifre", Toast.LENGTH_SHORT).show()
            }

        }
    }

}



