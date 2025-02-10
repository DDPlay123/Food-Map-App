package mai.project.foodmap.data.localDataSource

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromListString(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toListString(value: String): List<String> {
        return Json.decodeFromString(value)
    }
}