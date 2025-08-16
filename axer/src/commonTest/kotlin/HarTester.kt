import utils.TransactionFullTestUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HarFileGenerationTest {

    @Test
    fun `test single transaction HAR generation`() {
        val transaction = TransactionFullTestUtils.createTransaction()
        val har = TransactionFullTestUtils.createHarFromList(listOf(transaction))

        assertNotNull(har)
        assertEquals(1, har.log.entries.size)
        assertEquals(transaction.method, har.log.entries.first().request.method)
    }

    @Test
    fun `test multiple transactions HAR generation`() {
        val transactions = TransactionFullTestUtils.createTransactionList(
            count = 100,
            method = "POST",
            requestBodySize = 1024 * 100,
            responseBodySize = 1024 * 100
        )
        val har = TransactionFullTestUtils.createHarFromList(transactions)

        assertNotNull(har)
        assertEquals(100, har.log.entries.size)
        assertEquals("POST", har.log.entries.first().request.method)
    }

    @Test
    fun `test large transaction HAR generation`() {
        val transaction = TransactionFullTestUtils.createLargeTransaction(
            requestBodySize = 5_000_000,
            responseBodySize = 5_000_000
        )
        val har = TransactionFullTestUtils.createHarFromList(listOf(transaction))

        assertNotNull(har)
        assertEquals(1, har.log.entries.size)
        assertEquals(5_000_000, har.log.entries.first().request.bodySize)
        assertEquals(5_000_000, har.log.entries.first().response.content.size)
    }
}