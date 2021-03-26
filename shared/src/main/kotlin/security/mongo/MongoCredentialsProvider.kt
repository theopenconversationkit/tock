package ai.tock.shared.security.mongo

import com.mongodb.MongoCredential

/**
 * Mongo credential provider
 */
interface MongoCredentialsProvider {
    fun getCredentials(): MongoCredential?
}
