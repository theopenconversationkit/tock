package ai.tock.shared.security

import ai.tock.shared.error
import mu.KotlinLogging
import java.io.File
import java.security.cert.X509Certificate
import java.security.KeyStore
import java.io.FileOutputStream
import java.security.cert.CertificateFactory
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.io.FileInputStream


private const val KEY_STORE_TYPE = "JKS"
private const val KEY_STORE_PROVIDER = "SUN"
private const val KEY_STORE_FILE_PREFIX = "sys-connect-via-ssl-test-cacerts"
private const val KEY_STORE_FILE_SUFFIX = ".jks"
private const val DEFAULT_KEY_STORE_PASSWORD = "changeit"

private val logger = KotlinLogging.logger {}

/**
 * Set SSL property to integrate the PEM certificate.
 * @param pemCertificateName PEM certificate name, add it to the classpath.
 * @return
 */
internal fun setSslProperties(pemCertificateName: String){
    try {
        createKeyStoreFile(pemCertificateName)?.let {
            System.setProperty("javax.net.ssl.trustStore", it)
        }
    } catch (e: Exception) {
        logger.error(e)
    }

    System.setProperty("javax.net.ssl.trustStoreType", KEY_STORE_TYPE)
    System.setProperty("javax.net.ssl.trustStorePassword", DEFAULT_KEY_STORE_PASSWORD)
}

/**
 * For development purposes.
 * Allow unsecure SSL connexion, use it if you connect to a mongo database though an SSL tunnelling for instance.
 */
internal fun getNoopSslContext(): SSLContext {
    val sslContext: SSLContext
    try {
        sslContext = SSLContext.getInstance("SSL")

        // set up a TrustManager that trusts everything
        sslContext.init(null, arrayOf<TrustManager>(object : X509TrustManager {

            @Throws(CertificateException::class)
            override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, s: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf<X509Certificate>()

        }), SecureRandom())
    } catch (e: NoSuchAlgorithmException) {
        logger.error("Couldn't create SSL Context for MongoDB connection")
        throw RuntimeException(e)
    } catch (e: KeyManagementException) {
        logger.error("Couldn't create SSL Context for MongoDB connection")
        throw RuntimeException(e)
    }

    return sslContext
}

/**
 * Create the KeyStore file from the PEM certificate name.
 * @param pemCertificateName PEM certificate name, add it to the classpath.
 */
@Throws(Exception::class)
private fun createKeyStoreFile(pemCertificateName: String): String? {
    return createKeyStoreFile(createCertificate(pemCertificateName))?.path
}

/**
 * This method generates the SSL certificate from the PEM ressource name.
 * @param pemCertificateName PEM certificate ressource name. Add it to the classpath.
 * @return
 * @throws Exception
 */
@Throws(Exception::class)
private fun createCertificate(pemCertificateName: String): X509Certificate {
    val certFactory = CertificateFactory.getInstance("X.509")
    val url = File(pemCertificateName).toURI().toURL() ?: throw Exception()
    url.openStream().use { certInputStream -> return certFactory.generateCertificate(certInputStream) as X509Certificate }
}

/**
 * This method creates the Key Store File.
 * @param rootX509Certificate - the SSL certificate to be stored in the KeyStore
 * @return the created keystore file.
 */
private fun createKeyStoreFile(rootX509Certificate: X509Certificate): File? {
    val keyStoreFile = File.createTempFile(KEY_STORE_FILE_PREFIX, KEY_STORE_FILE_SUFFIX)
    FileOutputStream(keyStoreFile.getPath()).use { fos ->
        val ks = KeyStore.getInstance(KEY_STORE_TYPE, KEY_STORE_PROVIDER)
        val rootCAFilePath = System.getProperty("java.home") + "/lib/security/cacerts"
        FileInputStream(rootCAFilePath).use { fis ->
            ks.load(fis, "changeit".toCharArray())
        }
        ks.setCertificateEntry("rootCaCertificate", rootX509Certificate)
        ks.store(fos, DEFAULT_KEY_STORE_PASSWORD.toCharArray())
    }
    return keyStoreFile
}