package br.edu.fsa.planner.data.local.dao

import androidx.room.*
import br.edu.fsa.planner.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY fetchedAt DESC, id") fun observe(): Flow<List<QuoteEntity>>
    @Query("SELECT * FROM quotes ORDER BY id") suspend fun all(): List<QuoteEntity>
    @Query("SELECT * FROM quotes WHERE id = :id") suspend fun get(id: String): QuoteEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun seed(quotes: List<QuoteEntity>)
    @Upsert suspend fun put(quote: QuoteEntity)
    @Query("UPDATE quotes SET isFavorite = NOT isFavorite WHERE id = :id") suspend fun toggleFavorite(id: String)
    @Query("DELETE FROM quotes WHERE id = :id AND source = 'USER'") suspend fun deleteOwn(id: String)
    @Query("DELETE FROM quotes WHERE source = 'API' AND isFavorite = 0 AND id != :currentId AND id NOT IN (SELECT id FROM quotes WHERE source = 'API' ORDER BY fetchedAt DESC, id LIMIT 200)")
    suspend fun trimCache(currentId: String)
    @Transaction suspend fun merge(quotes: List<QuoteEntity>) {
        quotes.forEach { quote -> put(quote.copy(isFavorite = get(quote.id)?.isFavorite ?: false)) }
    }
}
