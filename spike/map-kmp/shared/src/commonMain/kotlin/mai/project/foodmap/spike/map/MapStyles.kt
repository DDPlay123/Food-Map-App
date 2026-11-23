package mai.project.foodmap.spike.map

/**
 * 深色模式地圖樣式（Google Maps Styling JSON）。兩個平台共用同一份，確保外觀一致。
 */
object MapStyles {
    val NIGHT_JSON: String = """
        [
          {"elementType":"geometry","stylers":[{"color":"#242f3e"}]},
          {"elementType":"labels.text.stroke","stylers":[{"color":"#242f3e"}]},
          {"elementType":"labels.text.fill","stylers":[{"color":"#746855"}]},
          {"featureType":"administrative.locality","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},
          {"featureType":"poi","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},
          {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#263c3f"}]},
          {"featureType":"road","elementType":"geometry","stylers":[{"color":"#38414e"}]},
          {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#212a37"}]},
          {"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#9ca5b3"}]},
          {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#746855"}]},
          {"featureType":"transit","elementType":"geometry","stylers":[{"color":"#2f3948"}]},
          {"featureType":"water","elementType":"geometry","stylers":[{"color":"#17263c"}]},
          {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#515c6d"}]}
        ]
    """.trimIndent()
}
