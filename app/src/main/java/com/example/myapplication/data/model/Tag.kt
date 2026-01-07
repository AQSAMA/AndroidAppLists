package com.example.myapplication.data.model

/**
 * Represents a custom tag/label that can be assigned to apps within lists
 */
data class Tag(
    val id: Long = 0,
    val name: String,
    val color: Long = DEFAULT_COLOR
) {
    companion object {
        const val DEFAULT_COLOR = 0xFF6200EE // Purple
        
        // Predefined tag colors
        val PRESET_COLORS = listOf(
            0xFF6200EE, // Purple
            0xFF03DAC5, // Teal
            0xFFFF5722, // Deep Orange
            0xFF4CAF50, // Green
            0xFF2196F3, // Blue
            0xFFE91E63, // Pink
            0xFFFF9800, // Orange
            0xFF9C27B0, // Deep Purple
            0xFF795548, // Brown
            0xFF607D8B  // Blue Grey
        )
    }
}
