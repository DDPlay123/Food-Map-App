package mai.project.foodmap.spike.map

import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 驗證用的假資料。店名都是虛構的，位置分布在台北 101 一帶（對應 App 的預設座標）。
 */
object SampleData {

    /** App 的預設座標（core/Configs.kt 的 DEFAULT_LATITUDE / DEFAULT_LONGITUDE）。 */
    val center = GeoPoint(25.032972653092518, 121.56537596752182)

    private val categories = listOf("牛肉麵", "拉麵", "咖啡廳", "早午餐", "火鍋", "便當", "甜點", "燒肉")

    /**
     * 60 家散布的餐廳，加上 6 家座標完全相同的「美食街」餐廳。
     * 後者放到最大也不會分開，用來驗證「已達最大縮放時列出群集清單」的流程。
     */
    val restaurants: List<RestaurantMarker> = buildList {
        val random = Random(7)
        repeat(60) { i ->
            add(
                RestaurantMarker(
                    id = "r" + (i + 1).toString().padStart(2, '0'),
                    name = "示範${categories[i % categories.size]} ${i + 1}",
                    position = GeoPoint(
                        latitude = center.latitude + random.nextDouble(-0.012, 0.012),
                        longitude = center.longitude + random.nextDouble(-0.014, 0.014),
                    ),
                    rating = (random.nextDouble(3.2, 4.9) * 10).roundToInt() / 10.0,
                ),
            )
        }
        val foodCourt = GeoPoint(25.03395, 121.56430)
        repeat(6) { i ->
            add(
                RestaurantMarker(
                    id = "fc${i + 1}",
                    name = "示範美食街 ${categories[i]}",
                    position = foodCourt,
                    rating = if (i == 0) null else 3.5 + i * 0.2,
                ),
            )
        }
    }

    /**
     * 範例路線：台北 101 → 市政府站，以 Encoded Polyline 表示，和後端回傳的格式相同。
     */
    const val ENCODED_ROUTE = "_kxwCca~dV?wQ_IIgJI{JI{JISbGg@bG"
}
