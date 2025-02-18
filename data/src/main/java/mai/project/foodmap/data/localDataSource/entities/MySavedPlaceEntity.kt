package mai.project.foodmap.data.localDataSource.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import mai.project.foodmap.data.localDataSource.MY_SAVED_PLACE
import mai.project.foodmap.domain.models.MyPlaceResult

@Entity(tableName = MY_SAVED_PLACE)
internal data class MySavedPlaceEntity(
    @PrimaryKey
    val index: String,
    @Embedded
    val result: MyPlaceResult
)
