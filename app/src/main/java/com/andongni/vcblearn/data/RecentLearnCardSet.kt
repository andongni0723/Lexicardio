package com.andongni.vcblearn.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecentLearnCardSet(
    @SerialName("name")
    val name: String = "",
    @SerialName("uri")
    val uri: String = "",
    @SerialName("last_learned_at")
    val lastLearnedAt: Long = 0L
)
