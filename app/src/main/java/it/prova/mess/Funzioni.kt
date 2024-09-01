package it.prova.mess

import android.app.Activity
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64.DEFAULT
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.mindrot.jbcrypt.BCrypt
import java.io.*
import java.math.BigInteger
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.security.spec.MGF1ParameterSpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.*
import kotlin.io.encoding.ExperimentalEncodingApi


fun String.hash(salt: String): String{
    var hash = BCrypt.hashpw(this, salt)
    return hash
}

@OptIn(ExperimentalEncodingApi::class)
fun decifraAES(Cifrato: String, chiave: String): String {
        val cifrario = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cifrario.init(Cipher.DECRYPT_MODE,SecretKeySpec(chiave.toByteArray(), "AES"))
        val testo = cifrario.doFinal(kotlin.io.encoding.Base64.decode(Cifrato, DEFAULT))
        return testo.decodeToString()
}


@RequiresApi(Build.VERSION_CODES.O)
fun RSA(Plain: String): String {
    val cifrario: Cipher = Cipher.getInstance("RSA/None/PKCS1Padding")
    val ChiaveEsterna = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnGcaITCBd58dvDzJa2Hwr0QAIEEJpiJoaa+HPX82MnfYOu/fRTJySojAcI5wcL10HT9Du5JV3dXSYxwjO3QPV8lTq/eJy66lZHhUOtcVzhcBZ1s81LH+A+nmH6l2CvyzXK8THuB7m7dMz8ObDySHQc24/PdFTpkHGIyweSHi9ow1R71czkjRtgsFMbvVVwcetH/3RPKmbbO65wS6eTXcN1B2keC9x0v48oDm9p6+bHhPT/09FFNykZKKb+n38cMnBV2S8/daPBpzuf2q5hNO2EexA9h/wmW2pCWxFq13TXvUlq6HU6TpnV9qzY1b0vUpSIsXq3d+dfCYfV2R+/qbqwIDAQAB"
    val chiavees = android.util.Base64.decode(ChiaveEsterna, DEFAULT)
    val contenitore = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(chiavees)) as RSAPublicKey
    val modulo = contenitore.modulus.toString(16)
    val esponente = contenitore.publicExponent.toString(16)
    var ChiavePubblicaServer = ChiavePubRSA(modulo, esponente)
    cifrario.init(Cipher.ENCRYPT_MODE, ChiavePubblicaServer)
    val cifrato =  cifrario.doFinal(Plain.toByteArray())
    return android.util.Base64.encodeToString(cifrato, DEFAULT)
}

@OptIn(ExperimentalEncodingApi::class)
@RequiresApi(Build.VERSION_CODES.O)
fun cifraAES(chiaro: String): Pair<String?, String> {
    var keyStringa = ""
    for (i in 1..16) {
        keyStringa += chiaro[(0..chiaro.length - 1).random()]
    }
    val plainText = chiaro.toByteArray(Charsets.UTF_8)
    val keyinByte = keyStringa.toByteArray()
    val key = SecretKeySpec(keyinByte, "AES")
    val cifrario = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cifrario.init(Cipher.ENCRYPT_MODE, key)
    val cifrato = cifrario.doFinal(plainText)
    val d = android.util.Base64.encodeToString(cifrato, DEFAULT)
    return Pair(d, keyStringa)
}


fun ChiaveRSA(): KeyPair{
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    if (!keyStore.containsAlias("RSA-chiave")) {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        val SpecificheGen = KeyGenParameterSpec.Builder("RSA-chiave",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setKeySize(2048)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .build()
        keyPairGenerator.initialize(SpecificheGen)
        return keyPairGenerator.generateKeyPair()
    } else {
        val privateKey = keyStore.getKey("RSA-chiave", null) as PrivateKey
        val publicKey = keyStore.getCertificate("RSA-chiave").publicKey
        return KeyPair(publicKey, privateKey)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun ChiavePubRSA(modulo: String, esponente: String): PublicKey {
    val modulus = BigInteger( modulo, 16)
    val exponent = BigInteger( esponente, 16)
    val keySpec = RSAPublicKeySpec(modulus, exponent)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePublic(keySpec)
}

@RequiresApi(Build.VERSION_CODES.O)
fun RRSSA(){
    val cifrario: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    val Testo = "C0qUAe4x76N+SIHWS4RiuN7A08KWEAXm9T2oK1mBeUg0CHjYBxlVcC/roSl0OSqaktwhYBnSO1hZlb0PycN3q3AaBWDewSBYeIb25iQtpimUY2aibCaoogzDa70yEgmIUabic5AbDRiHxpnD9zKrLbp3+sf6bnAZ4x42gLQYe/S/js6xePnyyfoBXXTB2Ek0ZBhx0OS3H37xs+vKVcx19oQAQfPxZfKpIkqjfqmq90i9o5YqOfN/UxNT+22UvoqTMFXeIbpfa5a48k0MIjfBqJQQ5gjABuASi/6V59qB57/UrDhHj06HPuT8uBsAT5y/9ojWaVpZVYLxtRZy/jMByQ=="
    val ChiaveEsterna = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhI/4hnxT2KQkmhl4RIzPeS1lRtxGq5A+IxUl0x8mBXRydSjthZ0jhDXjLNiqX1JXobhut1FWYGU+fwpTxBgOElv6A+XjqbqItrLwyK/dQMz6G1rMxxgi7jck8u88yqUUOaJjDl56bGTCmxJkjn4ROv5+d9Ixs1+qyefabotjv4vduhGZPSXz8014ExLJaln88ZL8piIx9NTsONlD4UOEFQzFrq5w0sy4VgPt0bMaBaFITa3JJnX4KynXk4PtPVj3VIdM/IntjIXOpVq5LxFB9dXDW3pcTkThn03Va2di3Rmwmq8OJfs0zp6nI3tG8ifXBGTpgpHBezuTFHVj3MT2rwIDAQAB"
    val chiavees = Base64.getDecoder().decode(ChiaveEsterna)
    val contenitore = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(chiavees)) as RSAPublicKey
    val modulo = contenitore.modulus.toString(16)
    val esponente = contenitore.publicExponent.toString(16)
    println("modulo e esponente:\n $modulo\n$esponente")
    var publicKey = ChiavePubRSA(modulo, esponente)
    cifrario.init(Cipher.DECRYPT_MODE, publicKey)
    var t = Base64.getDecoder().decode(Testo)
    var testo = cifrario.doFinal(t)
    println("testo in chiaro:")
    println(testo.decodeToString())

}






@RequiresApi(Build.VERSION_CODES.O)
fun cifraRSA(testo: String): String {
    val chiavi = ChiaveRSA()
    val cifrario = Cipher.getInstance("RSA/ECB/OAEPwithSHA-256andMGF1Padding")
    val spec = OAEPParameterSpec(
        "SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT
    )
    cifrario.init(Cipher.ENCRYPT_MODE, chiavi.public, spec)
    val cifrato = cifrario.doFinal(testo.toByteArray())
    return android.util.Base64.encodeToString(cifrato, DEFAULT)
}


@RequiresApi(Build.VERSION_CODES.O)
fun decifraRSA(cifrato: String): String {
    val chiavi = ChiaveRSA()
    val spec = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)
    val cifrario = Cipher.getInstance("RSA/ECB/OAEPwithSHA-256andMGF1Padding")
    cifrario.init(Cipher.DECRYPT_MODE, chiavi.private, spec)
    return String(cifrario.doFinal(android.util.Base64.decode(cifrato, DEFAULT)))
}






@RequiresApi(Build.VERSION_CODES.O)
fun cifraMessaggioAES(chiaro: String, chiave: String): String? {
    println("chiave: $chiave")
    val plainText = chiaro.toByteArray(Charsets.UTF_8)
    val cifrario = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val chiave_ci = SecretKeySpec(chiave.toByteArray(), "AES")
    cifrario.init(Cipher.ENCRYPT_MODE, chiave_ci)
    val cifrato = cifrario.iv + cifrario.doFinal(plainText)
    return String(cifrato, Charsets.ISO_8859_1)
}

@RequiresApi(Build.VERSION_CODES.O)
fun decifraMessaggioAES(cifrato: ByteArray, chiave: String?): String? {
    val iv = cifrato.copyOfRange(0,16)
    val testoCifrato = cifrato.copyOfRange(16, cifrato.size)
    val cifrario = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cifrario.init(Cipher.DECRYPT_MODE,SecretKeySpec(chiave!!.toByteArray(), "AES"), IvParameterSpec(iv))
    val testo = cifrario.doFinal(testoCifrato)
    return testo.decodeToString()
}

@RequiresApi(Build.VERSION_CODES.O)
fun PasswordDatabase(activity: Activity): String {
    val salvati = activity.getPreferences( Context.MODE_PRIVATE)
    val pass_cif = salvati.getString("PassDB", null)
    var pass: String
    if(pass_cif.isNullOrEmpty()){
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        pass = (1..32)
            .map { allowedChars.random() }
            .joinToString("")
        salvati.edit().putString("PassDB", cifraRSA(pass))
    } else{
         pass = decifraRSA(pass_cif)
    }
    return pass
}