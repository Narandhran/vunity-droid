package com.vunity.video


data class ReqVideoBody(
    var author: String,
    var categoryId: String,
    var description: String,
    var genre: List<Any>,
    var keywords: List<Any>,
    var name: String,
    var yearOfPublish: String,
    var makeAnnouncement: Boolean,
    var content: String
)

data class HomeDto(
    var contentFound: Boolean,
    var `data`: List<HomeData>,
    var message: String,
    var status: Int
)

data class HomeData(
    var books: List<HomeVideoData>,
    var genre: String
)

data class HomeVideoData(
    var _id: String,
    var author: String,
    var content: String,
    var description: String,
    var genre: String,
    var name: String,
    var thumbnail: String,
    var yearOfPublish: String
)

data class ReqSingleVideoBody(
    var libraryId: String,
    var userId: Any
)

data class VideoDto(
    var contentFound: Boolean,
    var `data`: VideoData,
    var message: String,
    var status: Int
)

data class VideoListDto(
    var contentFound: Boolean,
    var `data`: List<VideoData>,
    var message: String,
    var status: Int
)

data class VideoData(
    var __v: Int?,
    var _id: String?,
    var author: String?,
    var categoryId: CategoryId?,
    var content: String?,
    var createdAt: String?,
    var description: String?,
    var genre: List<String>?,
    var isBookmark: Boolean?,
    var keywords: List<String>?,
    var name: String?,
    var thumbnail: String?,
    var updatedAt: String?,
    var yearOfPublish: String?
)

data class CategoryId(
    var __v: Int?,
    var _id: String?,
    var createdAt: String?,
    var description: String?,
    var name: String?,
    var thumbnail: String?,
    var updatedAt: String?
)