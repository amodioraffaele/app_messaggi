package it.prova.mess

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import java.util.concurrent.TimeUnit


class Verifica : AppCompatActivity() {
    private var auth = FirebaseAuth.getInstance()
    private val Stringa = "È stato inviato un OTP al tuo numero "
    private var storedVerificationId : String? = ""
    private lateinit var Token : PhoneAuthProvider.ForceResendingToken
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
            .setPhoneNumber("$prefisso$numero") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(opzioni)
        val invia = findViewById<Button>(R.id.Invia)
        countdown()
        val rimanda = findViewById<TextView>(R.id.RimandaOTP)
        rimanda.setOnClickListener(){
            if (timer == 0){
                countdown()
                resendVerificationCode(numero.toString(), Token)
            }
        }
        invia.setOnClickListener(){
            val ViewOTP = findViewById<EditText>(R.id.OTP)
            val OTP = ViewOTP.text.toString()
            if (storedVerificationId == null && savedInstanceState != null) {
                onRestoreInstanceState(savedInstanceState);
            }
            val credential = PhoneAuthProvider.getCredential(storedVerificationId.toString(), OTP)
            signInWithPhoneAuthCredential(credential)

        }


    }

    private fun countdown() {
        timer = 60
        val count = findViewById<TextView>(R.id.Countdown)
        object :CountDownTimer(60*1000,1000){
            override fun onTick(millisecondi_rimanenti: Long){
                count.setText("${millisecondi_rimanenti/1000}")
            }

            override fun onFinish() {
                count.setText("0")
                timer = 0
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("prefisso", prefisso)
                    intent.putExtra("numero", numero)
                    startActivity(intent)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val errore = findViewById<TextView>(R.id.Errore)
                        val stringa = "OTP errato. Hai altri ${3-tentativi} a disposizione"
                        errore.setText(stringa)
                        errore.visibility = View.VISIBLE
                    }
                    // Update UI
                }
            }
    }
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

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
    private fun resendVerificationCode(phoneNumber: String, token: ForceResendingToken, ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,  // Phone number to verify
            60,  // Timeout duration
            TimeUnit.SECONDS,  // Unit of timeout
            this@Verifica,  // Activity (for callback binding)
            callbacks,  // OnVerificationStateChangedCallbacks
            token
        ) // ForceResendingToken from callbacks
    }

}

