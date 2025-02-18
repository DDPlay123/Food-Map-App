package mai.project.foodmap.data.localDataSource.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import mai.project.foodmap.data.localDataSource.MY_SEARCH_RESTAURANT
import mai.project.foodmap.domain.models.SearchRestaurantResult

@Entity(tableName = MY_SEARCH_RESTAURANT)
internal data class MySearchEntity(
    @PrimaryKey
    val index: String,
    @Embedded
    val result: SearchRestaurantResult
)