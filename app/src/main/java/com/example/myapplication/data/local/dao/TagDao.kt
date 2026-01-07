package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long
    
    @Update
    suspend fun updateTag(tag: TagEntity)
    
    @Delete
    suspend fun deleteTag(tag: TagEntity)
    
    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: Long)
    
    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): TagEntity?
    
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTagsFlow(): Flow<List<TagEntity>>
    
    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchTags(query: String): Flow<List<TagEntity>>
    
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?
}
