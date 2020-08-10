package com.vunity.favourite

data class FavListDto(
    var contentFound: Boolean,
    var `data`: List<FavData>,
    var message: String,
    var status: Int
)

data class FavData(
    var __v: Int,
    var _id: String,
    var createdAt: String,
    var isBookmark: Boolean,
    var libraryId: LibraryId,
    var updatedAt: String,
    var userId: String
)

data class LibraryId(
    var __v: Int,
    var _id: String,
    var author: String,
    var categoryId: String,
    var content: String,
    var createdAt: String,
    var description: String,
    var genre: List<String>,
    var keywords: List<String>,
    var name: String,
    var thumbnail: String,
    var updatedAt: String,
    var yearOfPublish: String
)