package com.vunity.category

data class CategoryDto(
    var contentFound: Boolean,
    var `data`: CategoryData,
    var message: String,
    var status: Int
)

data class CategoryBody(
    var description: String,
    var name: String
)

data class CategoryListDto(
    var contentFound: Boolean,
    var `data`: List<CategoryData>,
    var message: String,
    var status: Int
)

data class CategoryData(
    var __v: Int,
    var _id: String,
    var createdAt: String,
    var description: String,
    var name: String,
    var thumbnail: String,
    var updatedAt: String
)

data class GenreDto(
    var contentFound: Boolean,
    var `data`: List<GenreData>,
    var message: String,
    var status: Int
)

data class GenreData(
    var __v: Int,
    var _id: String,
    var genre: String
)