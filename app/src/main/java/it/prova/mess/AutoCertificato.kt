package it.prova.mess

import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.FileInputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Security
import java.security.cert.Certificate

/*lass AutoCertificato {
    private fun PrendiCertificatoSubordinato(PathFile: String): String{
        return try{
            val
        }
    }
    private fun PrendiChiave(PathFile: String): String{

    }
    private fun Salva(){

    }
    private fun GeneraChiavePem(encoded: ByteArray): String{

    }
    private fun GeneraCertificatoPem(encoded: ByteArray) {

    }
    private fun GeneraChiavi(): KeyPair{
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }
    private fun GeneraCertificateSigningRequest(
        udid: String,
        serialNumber: BigInteger,
        keyPair: KeyPair,
        first: Char
    ): X509CertificateHolder?{

    }
    private fun GeneraCertificatoClient(udid: String, csr: Any, first: Char): Certificate{

    }
    fun genera(udid: String, serialNumber: BigInteger, PathFile: String){
        Security.addProvider(BouncyCastleProvider())

        val subCaCertificateResult = PrendiCertificato(PathFile)
        if (subCaCertificateResult.second != null){
            return subCaCertificateResult.second
        }

        val subCaPrivateKeyResult = PrendiChiave(PathFile)
        if (subCaPrivateKeyResult.second != null){
            return  subCaPrivateKeyResult.second
        }

        val keyPair = GeneraChiavi()
        val csr = GeneraCertificateSigningRequest(udid, serialNumber, keyPair, subCaCertificateResult.first) ?: return ErrorCode.INTERNAL_SERVER_ERROR

        val clientCertificate = GeneraCertificatoClient(udid, csr, subCaPrivateKeyResult.first)
        val certificatePem = GeneraCertificatoPem(clientCertificate.encoded)
        val privateKeyPem = GeneraChiavePem(keyPair.private.encoded)

        Salva("$udid.pem.crt", certificatePem)
        Salva("$udid.pem.key", privateKeyPem)
    }
}*/