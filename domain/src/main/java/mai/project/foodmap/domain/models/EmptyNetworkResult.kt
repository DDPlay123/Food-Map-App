package mai.project.foodmap.domain.models

/**
 * 空的 NetworkResult 資料模型
 *
 * - 只包含 status 欄位，其他欄位皆為 null
 */
data class EmptyNetworkResult(
    val status: Int,
)