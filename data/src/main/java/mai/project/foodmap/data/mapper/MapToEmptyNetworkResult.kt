package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.remoteDataSource.models.BaseResponse
import mai.project.foodmap.data.utils.mapResult
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.state.NetworkResult

/**
 * 將 NetworkResult 結果 轉換為 EmptyNetworkResult
 */
internal fun <T> NetworkResult<T>.mapToEmptyNetworkResult(): NetworkResult<EmptyNetworkResult> {
    return mapResult { data ->
        if (data is BaseResponse) EmptyNetworkResult(data.status) else null
    }
}