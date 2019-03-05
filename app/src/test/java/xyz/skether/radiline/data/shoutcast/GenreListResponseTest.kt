package xyz.skether.radiline.data.shoutcast

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GenreListResponseTest {

    @Test
    fun deserialization() {
        val responseStream = javaClass.getResourceAsStream("/response-genre-list.xml")!!
        val result = GenreListResponse.Deserializer.deserialize(responseStream)
        val sampleGenres = listOf(
            GenreResponse(
                id = 25,
                name = "Acoustic Blues",
                hasChildren = false,
                parentId = 24
            ),
            GenreResponse(
                id = 31,
                name = "Cajun and Zydeco",
                hasChildren = false,
                parentId = 24
            ),
            GenreResponse(
                id = 26,
                name = "Chicago Blues",
                hasChildren = false,
                parentId = 24
            ),
            GenreResponse(
                id = 27,
                name = "Contemporary Blues",
                hasChildren = false,
                parentId = 24
            )
        )
        val sampleResponse = GenreListResponse(sampleGenres)
        assertThat(result).isEqualTo(sampleResponse)
    }

}
