package mai.project.foodmap.data.localDataSource.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import mai.project.foodmap.data.localDataSource.MY_FAVORITE_RESTAURANT
import mai.project.foodmap.domain.models.MyFavoriteResult

@Entity(tableName = MY_FAVORITE_RESTAURANT)
internal data class MyFavoriteEntity(
    @PrimaryKey
    val index: String,
    @Embedded
    val result: MyFavoriteResult
)
