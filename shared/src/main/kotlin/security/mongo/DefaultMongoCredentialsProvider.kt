package ai.tock.shared.security.mongo

import com.mongodb.MongoCredential

/**
 * Default Mongo credential provider with no authentication
 */
internal object DefaultMongoCredentialsProvider : MongoCredentialsProvider {
    override fun getCredentials(): MongoCredential? {
        return null
    }
}
