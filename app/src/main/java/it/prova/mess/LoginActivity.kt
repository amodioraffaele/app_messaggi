package it.prova.mess

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.widget.*
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.RequiresApi
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import java.util.concurrent.TimeUnit




class LoginActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        supportActionBar!!.hide()
        var accedi = findViewById<Button>(R.id.Accedi)
        val PrefissoView = findViewById<Spinner>(R.id.scelta1)
        PrefissoView.adapter = ArrayAdapter.createFromResource(this, R.array.prefissi, R.layout.spinnerlayout)
        var numeroView = findViewById<EditText>(R.id.editTextPhone)
        var passwordView = findViewById<EditText>(R.id.password)
        var Reg = findViewById<TextView>(R.id.regis)
        val n = intent.getStringExtra("numero")
        if (n != null){
            numeroView.setText(n)
            PrefissoView.setSelection(intent.getIntExtra("prefisso", 1))
        }
        accedi.setOnClickListener() {
            val numero = numeroView.text.toString()
            val password = passwordView.text.toString()
            passwordView.setText("")
            var prefisso = PrefissoView.selectedItem.toString()
            var risultato = ""
            runBlocking {
                risultato = Reg_E_Login("Login:", prefisso, numero, password, this@LoginActivity).toString()
        }
            Controllo(prefisso,numero, risultato)

        }
        Reg.setOnClickListener() {
            val intent = Intent(this, Registrazione::class.java)
            startActivity(intent)
        }


    }

    private fun Controllo(prefisso: String, numero: String, risultato: String) {
        val errore = findViewById<TextView>(R.id.Errori)
        errore.setText("")
        if (risultato == "Successo") {
            val intent = Intent(this@LoginActivity, Verifica::class.java)
            intent.putExtra("prefisso", prefisso)
            intent.putExtra("numero", numero)
            startActivity(intent)
        } else if (risultato == "Password errata") {
            errore.setText(risultato.lowercase())
        } else if (risultato == "Numero non trovato"){
            errore.setText(risultato.lowercase())
            errore.visibility
        }else {
            errore.setText("Si è verificato un errore, riprovare in seguito")
        }

    }
}

