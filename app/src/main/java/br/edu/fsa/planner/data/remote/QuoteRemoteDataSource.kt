package br.edu.fsa.planner.data.remote

import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

data class RemoteQuote(val text: String, val author: String, val rawCategory: String) {
    val id get() = "api-" + MessageDigest.getInstance("SHA-256").digest("$text\n$author".toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
interface QuoteRemoteDataSource { suspend fun fetch(): List<RemoteQuote> }

class HttpQuoteRemoteDataSource : QuoteRemoteDataSource {
    override suspend fun fetch(): List<RemoteQuote> = withContext(Dispatchers.IO) {
        val categories = get("categorias").getJSONArray("data")
        val known = (0 until categories.length()).map { categories.getString(it) }.toSet()
        val first = get("todas?pagina=1&limite=50").getJSONObject("data")
        val pages = first.optInt("paginas", 1).coerceIn(1, 4)
        val rows = (listOf(first) + (2..pages).map { get("todas?pagina=$it&limite=50").getJSONObject("data") })
            .flatMap { parseRows(it) }.filter { it.rawCategory in known }
        if (rows.isEmpty()) throw IOException("Empty quote collection")
        rows.distinctBy { it.id }
    }
    private fun get(endpoint: String): JSONObject {
        val connection = URL("$BASE/$endpoint").openConnection() as HttpsURLConnection
        try {
            connection.connectTimeout = 5000; connection.readTimeout = 5000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "Cadency/1.0 Android")
            if (connection.responseCode != 200) throw IOException("HTTP ${connection.responseCode}")
            val bytes = ByteArrayOutputStream()
            connection.inputStream.use { input ->
                val buffer = ByteArray(4096)
                while (true) { val count = input.read(buffer); if (count < 0) break
                    if (bytes.size() + count > 512 * 1024) throw IOException("Quote response too large")
                    bytes.write(buffer, 0, count)
                }
            }
            return JSONObject(bytes.toString("UTF-8")).also { if (!it.optBoolean("success")) throw IOException("API unsuccessful") }
        } finally { connection.disconnect() }
    }
    companion object {
        const val BASE = "https://ferramentasdaweb.com.br/mensagens-motivacionais/api/v1/frases.php"
        fun parseRows(data: JSONObject): List<RemoteQuote> {
            val rows = data.getJSONArray("frases")
            return (0 until rows.length()).mapNotNull { index ->
                val row = rows.getJSONObject(index)
                val text = row.optString("frase").trim()
                if (text.length !in 1..2000) null else RemoteQuote(text, row.optString("autor").take(200), row.optString("categoria").take(100))
            }
        }
    }
}
