package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.TagDao
import com.example.myapplication.data.local.entity.TagEntity
import com.example.myapplication.data.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    
    suspend fun createTag(name: String, color: Long = Tag.DEFAULT_COLOR): Long {
        val tag = TagEntity(name = name, color = color)
        return tagDao.insertTag(tag)
    }
    
    suspend fun updateTag(tag: TagEntity) {
        tagDao.updateTag(tag)
    }
    
    suspend fun deleteTag(tagId: Long) {
        tagDao.deleteTagById(tagId)
    }
    
    suspend fun getTagById(tagId: Long): TagEntity? {
        return tagDao.getTagById(tagId)
    }
    
    suspend fun getTagByName(name: String): TagEntity? {
        return tagDao.getTagByName(name)
    }
    
    fun getAllTagsFlow(): Flow<List<Tag>> {
        return tagDao.getAllTagsFlow().map { entities ->
            entities.map { it.toTag() }
        }
    }
    
    fun searchTags(query: String): Flow<List<Tag>> {
        return tagDao.searchTags(query).map { entities ->
            entities.map { it.toTag() }
        }
    }
    
    /**
     * Get or create a tag by name
     */
    suspend fun getOrCreateTag(name: String, color: Long = Tag.DEFAULT_COLOR): Long {
        val existing = tagDao.getTagByName(name)
        return existing?.id ?: createTag(name, color)
    }
    
    // ==================== Conversion ====================
    
    private fun TagEntity.toTag(): Tag {
        return Tag(
            id = id,
            name = name,
            color = color
        )
    }
}
