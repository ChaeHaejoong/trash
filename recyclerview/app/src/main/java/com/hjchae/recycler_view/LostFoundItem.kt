package com.hjchae.recycler_view

data class LostFoundItem(
    val id: Long,
    val type: String,
    val name: String,
    val place: String,
    val date: String,
    val status: String,
    val imageUri: Int,
    val ownerId: String,
    val createdAt: Long,
    val description: String
) {
    companion object {
        const val TYPE_LOST = "LOST"
        const val TYPE_FOUND = "FOUND"
        const val STATUS_OPEN = "OPEN"
        const val STATUS_COMPLETED = "COMPLETED"
    }
}
