package mai.project.foodmap.data.localDataSource.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import mai.project.foodmap.data.localDataSource.MY_BLOCK_RESTAURANT
import mai.project.foodmap.domain.models.RestaurantResult

@Entity(tableName = MY_BLOCK_RESTAURANT)
internal class MyBlockEntity(
    @PrimaryKey
    val index: String,
    @Embedded
    val result: RestaurantResult
)