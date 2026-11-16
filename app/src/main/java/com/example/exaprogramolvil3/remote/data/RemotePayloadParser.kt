package com.example.exaprogramolvil3.remote.data

import org.json.JSONObject

data class RemotePayload(
    val features: List<FeatureEntity>,
    val banners: List<BannerEntity>
)

object RemotePayloadParser {
    fun parse(json: String): RemotePayload {
        val root = JSONObject(json)
        val featureArray = root.getJSONArray("features")
        val bannerArray = root.getJSONArray("banners")
        val features = (0 until featureArray.length()).map { index ->
            val item = featureArray.getJSONObject(index)
            FeatureEntity(
                featureKey = item.getString("key"),
                label = item.getString("label"),
                enabled = item.getBoolean("enabled")
            )
        }
        val banners = (0 until bannerArray.length()).map { index ->
            val item = bannerArray.getJSONObject(index)
            BannerEntity(
                bannerId = item.getString("id"),
                title = item.getString("title"),
                subtitle = item.getString("subtitle")
            )
        }
        require(features.map { it.featureKey }.distinct().size == features.size) {
            "Las claves de features no pueden repetirse"
        }
        require(banners.map { it.bannerId }.distinct().size == banners.size) {
            "Los identificadores de banners no pueden repetirse"
        }
        return RemotePayload(features, banners)
    }
}
