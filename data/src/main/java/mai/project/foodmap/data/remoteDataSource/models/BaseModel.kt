package mai.project.foodmap.data.remoteDataSource.models

import mai.project.foodmap.data.annotations.StatusCode

/**
 * 基底 Request
 */
internal abstract class BaseRequest {
    abstract val accessKey: String
    abstract val userId: String
}

/**
 * 基底 Response
 */
internal abstract class BaseResponse {
    @StatusCode
    abstract val status: Int
    abstract val errMsg: String?
}