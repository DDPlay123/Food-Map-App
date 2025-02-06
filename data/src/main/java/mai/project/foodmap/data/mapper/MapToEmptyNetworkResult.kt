package mai.project.foodmap.data.mapper

import mai.project.foodmap.data.remoteDataSource.models.BaseResponse
import mai.project.foodmap.domain.models.EmptyNetworkResult
import mai.project.foodmap.domain.state.NetworkResult

/**
 * 將 NetworkResult 結果 轉換為 EmptyNetworkResult
 */
internal fun <T> NetworkResult<T>.mapToEmptyNetworkResult(): NetworkResult<EmptyNetworkResult> {
    val copyData = data
    val result = if (copyData is BaseResponse) EmptyNetworkResult(copyData.status) else null
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(result)
        is NetworkResult.Error -> NetworkResult.Error(message, result)
        is NetworkResult.AccessKeyIllegal -> NetworkResult.AccessKeyIllegal()
        is NetworkResult.Loading -> NetworkResult.Loading()
        is NetworkResult.Idle -> NetworkResult.Idle()
    }
}