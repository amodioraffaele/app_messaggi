package it.prova.mess

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import java.util.concurrent.TimeUnit


class Verifica : AppCompatActivity() {
    private var auth = FirebaseAuth.getInstance()
    private val Stringa = "È stato inviato un OTP al tuo numero "
    private var storedVerificationId : String? = ""
    private lateinit var Token : ForceResendingToken
    private var tentativi = 0
    private var timer = 60
    private lateinit var numero: String
    private lateinit var prefisso: String
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        numero = intent.getStringExtra("numero").toString()
        prefisso = intent.getStringExtra("prefisso").toString()
        setContentView(R.layout.activity_verifica)
        val Edit = findViewById<TextView>(R.id.Testo)
        Edit.setText("$Stringa ${prefisso}$numero")
        auth.useAppLanguage()
        val opzioni = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("$prefisso$numero")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(opzioni)
        val invia = findViewById<Button>(R.id.Invia)
        countdown()
        val rimanda = findViewById<TextView>(R.id.RimandaOTP)
        rimanda.setOnClickListener(){
            if (timer == 0){
                countdown()
                re_invia_codice(numero, Token)
            }
        }
        invia.setOnClickListener(){
            val ViewOTP = findViewById<EditText>(R.id.OTP)
            val OTP = ViewOTP.text.toString()
            if (storedVerificationId == null && savedInstanceState != null) {
                onRestoreInstanceState(savedInstanceState)
            }
            val credential = PhoneAuthProvider.getCredential(storedVerificationId.toString(), OTP)
            login(credential)

        }


    }

    private fun countdown() {
        timer = 60
        val testo_countdown = findViewById<TextView>(R.id.Countdown)
        val countdown = object :CountDownTimer(60*1000,1000){
            override fun onTick(millisecondi_rimanenti: Long){
                testo_countdown.setText("${millisecondi_rimanenti/1000}")
            }

            override fun onFinish() {
                testo_countdown.setText("0")
                timer = 0
            }
        }
        countdown.start()
    }

    private fun login(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("prefisso", prefisso)
                    intent.putExtra("numero", numero)
                    startActivity(intent)
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val errore = findViewById<TextView>(R.id.Errore)
                        val stringa = "OTP errato. Hai altri ${3-tentativi} a disposizione"
                        errore.setText(stringa)
                        errore.visibility = View.VISIBLE
                    }
                }
            }
    }
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) { //viene chiamato quando l'app prende l'otp da sola
            login(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) { //chiamato in caso la richiesta per la verifica non è valida
            Toast.makeText(this@Verifica, "Si è verificato un problema, riprovare in seguito", Toast.LENGTH_LONG).show() // quota degli sms raggiunta

        }

        override fun onCodeSent(
            verificationId: String,
            token: ForceResendingToken
        ) {
            storedVerificationId =  verificationId
            Token = token

        }
    }
    override fun onRestoreInstanceState( savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        storedVerificationId = savedInstanceState.getString("KEY_VERIFICATION_ID")
    }
    override fun onSaveInstanceState( outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("KEY_VERIFICATION_ID", storedVerificationId)
    }
    private fun re_invia_codice(numero: String, token: ForceResendingToken, ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            numero,
            60,
            TimeUnit.SECONDS,
            this@Verifica,
            callbacks,
            token
        )
    }

}

