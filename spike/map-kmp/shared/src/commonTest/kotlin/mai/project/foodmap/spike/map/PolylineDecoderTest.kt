package mai.project.foodmap.spike.map

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PolylineDecoderTest {

    @Test
    fun decodesGoogleReferenceExample() {
        // https://developers.google.com/maps/documentation/utilities/polylinealgorithm 的官方範例
        val points = PolylineDecoder.decode("_p~iF~ps|U_ulLnnqC_mqNvxq`@")
        assertEquals(3, points.size)
        assertNear(GeoPoint(38.5, -120.2), points[0])
        assertNear(GeoPoint(40.7, -120.95), points[1])
        assertNear(GeoPoint(43.252, -126.453), points[2])
    }

    @Test
    fun decodesSampleRoute() {
        val points = PolylineDecoder.decode(SampleData.ENCODED_ROUTE)
        assertEquals(8, points.size)
        assertNear(GeoPoint(25.03360, 121.56450), points.first())
        assertNear(GeoPoint(25.04110, 121.56510), points.last())
    }

    @Test
    fun emptyInputGivesEmptyList() {
        assertTrue(PolylineDecoder.decode("").isEmpty())
    }

    @Test
    fun truncatedInputFails() {
        assertFailsWith<IllegalArgumentException> { PolylineDecoder.decode("_p~iF~ps|U_") }
    }

    private fun assertNear(expected: GeoPoint, actual: GeoPoint) {
        assertTrue(
            abs(expected.latitude - actual.latitude) < 1e-6 && abs(expected.longitude - actual.longitude) < 1e-6,
            "expected $expected but was $actual",
        )
    }
}
