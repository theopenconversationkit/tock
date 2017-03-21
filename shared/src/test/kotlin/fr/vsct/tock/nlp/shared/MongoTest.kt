package fr.vsct.tock.nlp.shared

import fr.vsct.tock.shared.collectionBuilder
import org.junit.Test
import kotlin.test.assertEquals

/**
 *
 */
class MongoTest {

    class ThisIsACollection

    @Test
    fun collectionBuilder_shouldAddUnderscore_forEachUpperCase() {
        assertEquals("this_is_a_collection", collectionBuilder.invoke(ThisIsACollection::class))
    }
}