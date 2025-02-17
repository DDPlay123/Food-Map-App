package mai.project.foodmap.data.localDataSource.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import mai.project.foodmap.data.localDataSource.MY_BLOCKED_RESTAURANT
import mai.project.foodmap.domain.models.MyBlacklistResult

@Entity(tableName = MY_BLOCKED_RESTAURANT)
internal class MyBlacklistEntity(
    @PrimaryKey
    val index: String,
    @Embedded
    val result: MyBlacklistResult
)