package it.prova.mess


import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64.DEFAULT
import androidx.annotation.RequiresApi
import org.mindrot.jbcrypt.BCrypt
import java.io.*
import java.math.BigInteger
import java.security.*
import java.security.interfaces.RSAPublicKey
import java.security.spec.MGF1ParameterSpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.*
import kotlin.io.encoding.ExperimentalEncodingApi


fun String.hash(salt: String): String{
    val hash = BCrypt.hashpw(this, salt)
    return hash
}

@OptIn(ExperimentalEncodingApi::class)
fun decifraAES(Cifrato: String, chiave: String): String {
        val cifrario = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cifrario.init(Cipher.DECRYPT_MODE,SecretKeySpec(chiave.toByteArray(), "AES"))
        val testo = cifrario.doFinal(kotlin.io.encoding.Base64.decode(Cifrato, DEFAULT))
        return testo.decodeToString()
}


fun RSA(Plain: String): String {
    val cifrario: Cipher = Cipher.getInstance("RSA/None/PKCS1Padding")
    val ChiaveEsterna = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnGcaITCBd58dvDzJa2Hwr0QAIEEJpiJoaa+HPX82MnfYOu/fRTJySojAcI5wcL10HT9Du5JV3dXSYxwjO3QPV8lTq/eJy66lZHhUOtcVzhcBZ1s81LH+A+nmH6l2CvyzXK8THuB7m7dMz8ObDySHQc24/PdFTpkHGIyweSHi9ow1R71czkjRtgsFMbvVVwcetH/3RPKmbbO65wS6eTXcN1B2keC9x0v48oDm9p6+bHhPT/09FFNykZKKb+n38cMnBV2S8/daPBpzuf2q5hNO2EexA9h/wmW2pCWxFq13TXvUlq6HU6TpnV9qzY1b0vUpSIsXq3d+dfCYfV2R+/qbqwIDAQAB"
    val chiavees = android.util.Base64.decode(ChiaveEsterna, DEFAULT)
    val contenitore = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(chiavees)) as RSAPublicKey
    val modulo = contenitore.modulus.toString(16)
    val esponente = contenitore.publicExponent.toString(16)
    val ChiavePubblicaServer = ChiavePubRSA(modulo, esponente)
    cifrario.init(Cipher.ENCRYPT_MODE, ChiavePubblicaServer)
    val cifrato =  cifrario.doFinal(Plain.toByteArray())
    return android.util.Base64.encodeToString(cifrato, DEFAULT)
}

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
    return Pair(android.util.Base64.encodeToString(cifrato, DEFAULT), keyStringa)
}


@RequiresApi(Build.VERSION_CODES.M)
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

fun ChiavePubRSA(modulo: String, esponente: String): PublicKey {
    val modulus = BigInteger( modulo, 16)
    val exponent = BigInteger( esponente, 16)
    val keySpec = RSAPublicKeySpec(modulus, exponent)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePublic(keySpec)
}





@RequiresApi(Build.VERSION_CODES.M)
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


@RequiresApi(Build.VERSION_CODES.M)
fun decifraRSA(cifrato: String): String {
    val chiavi = ChiaveRSA()
    val spec = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)
    val cifrario = Cipher.getInstance("RSA/ECB/OAEPwithSHA-256andMGF1Padding")
    cifrario.init(Cipher.DECRYPT_MODE, chiavi.private, spec)
    return String(cifrario.doFinal(android.util.Base64.decode(cifrato, DEFAULT)))
}






fun cifraMessaggioAES(chiaro: String, chiave: String): String {
    val plainText = chiaro.toByteArray(Charsets.UTF_8)
    val cifrario = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val chiave_ci = SecretKeySpec(chiave.toByteArray(), "AES")
    cifrario.init(Cipher.ENCRYPT_MODE, chiave_ci)
    val cifrato = cifrario.iv + cifrario.doFinal(plainText)
    return String(cifrato, Charsets.ISO_8859_1)
}

fun decifraMessaggioAES(cifrato: ByteArray, chiave: String?): String {
    val iv = cifrato.copyOfRange(0,16)
    val testoCifrato = cifrato.copyOfRange(16, cifrato.size)
    val cifrario = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cifrario.init(Cipher.DECRYPT_MODE,SecretKeySpec(chiave!!.toByteArray(), "AES"), IvParameterSpec(iv))
    val testo = cifrario.doFinal(testoCifrato)
    return testo.decodeToString()
}

fun CancellaChiaveRSA(){
    val keystore : KeyStore  = KeyStore.getInstance("AndroidKeyStore")
    keystore.load(null)
    if (keystore.containsAlias("RSA-chiave")){
        keystore.deleteEntry("RSA-chiave")
    }
}