package it.prova.mess

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*


data class Cifratura(
    var cifrato : String,
    var ChiaveCifrata : String,
    val chiave : String
    )
@RequiresApi(Build.VERSION_CODES.O)
class API_SERVER {


    fun cifra(dati: String): Cifratura {
        val (cifrato, chiave) = cifraAES(dati)
        val ChiaveCifrata = RSA(chiave)
        return Cifratura(cifrato!!,ChiaveCifrata,chiave)
    }
    fun thread(tipo: String, cifrato: String, chiave: String) : String{
        val connessione = Connessione(tipo, cifrato, chiave)
        val thread = Thread(connessione)
        thread.start()
        thread.join()
        val risposta = connessione.Prendi()
        return risposta
    }
    fun login(prefisso: String, numero: String, password: String): String{
        val cifratura= cifra("$prefisso $numero $password")
        val risposta_cifrata = thread("/login", cifratura.cifrato, cifratura.ChiaveCifrata)
        if (risposta_cifrata.startsWith("Errore")) {
            return risposta_cifrata
        } else {
            val risposta = decifraAES(risposta_cifrata, cifratura.chiave)
            return risposta
        }
    }

    fun registrazione(prefisso: String, numero: String, password: String): String {
        val cifratura = cifra("$prefisso $numero $password")
        val risposta_cifrata = thread("/registrazione", cifratura.cifrato, cifratura.ChiaveCifrata)
        if (risposta_cifrata.startsWith("Errore") || risposta_cifrata.isNullOrEmpty()) {
            return risposta_cifrata
        } else {
            val risposta = decifraAES(risposta_cifrata, cifratura.chiave)
            return risposta
        }
    }

    fun cerca_numero(numero: String): String{
        val cifratura = cifra("$numero")
        val risposta_cifrata = thread( "/cerca_numero", cifratura.cifrato, cifratura.ChiaveCifrata)
        if (risposta_cifrata.startsWith("Errore")) {
            return risposta_cifrata
        } else {
            val risposta = decifraAES(risposta_cifrata, cifratura.chiave)
            return risposta
        }
    }

    fun cerca_id(id: String): String{
        val cifratura = cifra("$id")
        val risposta_cifrata = thread("/cerca_id", cifratura.cifrato, cifratura.ChiaveCifrata)
        if (risposta_cifrata.startsWith("Errore")) {
            return risposta_cifrata
        } else {
            val risposta = decifraAES(risposta_cifrata, cifratura.chiave)
            return risposta
        }
    }

    fun reg_id(prefisso: String, numero: String, id : String, password: String, activity: Activity) : String{
        val cifratura = cifra("$prefisso $numero $id $password")
        val risposta_cifrata = thread("/reg_id", cifratura.cifrato, cifratura.ChiaveCifrata)
        if (risposta_cifrata.startsWith("Errore")) {
            return risposta_cifrata
        } else {
            val risposta = decifraAES(risposta_cifrata, cifratura.chiave)
            val chiave_API = risposta.removePrefix("Successo API: ")
            val sharedPreferences = activity.getSharedPreferences("API_KEY", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()){
                putString("API_KEY", cifraRSA(chiave_API))
                apply()
            }
            return risposta.removeSuffix(chiave_API)
        }
    }

    fun chiave(id1: String, id2 : String, activity: Activity): String{
        val sharedPreferences = activity.getSharedPreferences("API_KEY", Context.MODE_PRIVATE)
        val chiave = sharedPreferences.getString("API_KEY", null)
        val chiave_dec = decifraRSA(chiave!!)
        val cifratura = cifra("$id1 $id2 $chiave_dec")
        val risposta_cifrata = thread("/chiave", cifratura.cifrato, cifratura.ChiaveCifrata)
        if (risposta_cifrata.startsWith("Errore")) {
            return risposta_cifrata
        } else {
            val risposta = decifraAES(risposta_cifrata, cifratura.chiave)
            return risposta
        }
    }


    private class Connessione : Runnable {
        @Volatile
        private var url = "https://299d-79-49-10-180.ngrok-free.app"
        var risposta = "errore"
        var cifrato : String? = null
        var chiave  : String? = null
        var tipo : String? = null
        constructor(tipo: String,cifrato: String, chiave: String){
            this.cifrato = cifrato
            this.tipo = tipo
            this.chiave = chiave
        }
        override fun run() {
            val client = OkHttpClient()
            val url_connessione = url + tipo
            var json = JsonObject()
            json.addProperty("cifratoAES", cifrato)
            json.addProperty("ChiaveCifrata", chiave)
            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url_connessione)
                .post(body)
                .build()

            try {
                val ok = client.newCall(request).execute()
                risposta = ok.body!!.string().substringAfter("\"risposta\":\"").substringBeforeLast("\"")
            } catch (e: Exception){
                risposta = "Errore durante la connessione"
            }
        }

        fun Prendi(): String {
            return this.risposta
        }


    }


}


