package mai.project.foodmap.spike.map

/**
 * 解碼 Google Encoded Polyline。
 *
 * 後端的 `api/geocode/get_route_polyline` 回傳這種格式；正式 App 目前用 geolib-polyline（只支援 Android）解碼。
 * 演算法說明：https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 */
object PolylineDecoder {

    fun decode(encoded: String): List<GeoPoint> {
        val points = ArrayList<GeoPoint>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            val dLat = readValue(encoded, index).also { index = it.second }.first
            val dLng = readValue(encoded, index).also { index = it.second }.first
            lat += dLat
            lng += dLng
            points += GeoPoint(lat / 1e5, lng / 1e5)
        }
        return points
    }

    /** 讀一個帶正負號的數值，回傳（數值, 下一個位置）。 */
    private fun readValue(encoded: String, start: Int): Pair<Int, Int> {
        var index = start
        var result = 0
        var shift = 0
        var b: Int
        do {
            require(index < encoded.length) { "Polyline 格式不完整：位置 $index" }
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val value = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        return value to index
    }
}
