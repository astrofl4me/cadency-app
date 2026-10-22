package br.edu.fsa.planner.data.local.entity

import androidx.room.*
import br.edu.fsa.planner.domain.model.*

@Entity(tableName = "quotes", indices = [Index("isFavorite"), Index("category")])
data class QuoteEntity(@PrimaryKey val id: String, val text: String, val author: String, val category: String,
    val rawCategory: String, val fetchedAt: Long, val isFavorite: Boolean, val source: String)

fun QuoteEntity.toModel() = CadencyQuote(id, text, author, QuoteCategory.valueOf(category), rawCategory, fetchedAt, isFavorite, QuoteSource.valueOf(source))
fun CadencyQuote.toEntity() = QuoteEntity(id, text, author, category.name, rawCategory, fetchedAt, isFavorite, source.name)
