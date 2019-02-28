package xyz.skether.radiline.domain

import xyz.skether.radiline.data.shoutcast.GenreResponse
import xyz.skether.radiline.data.shoutcast.StationResponse

data class Station(
    val id: Int,
    val name: String,
    val listeners: Int = 1234,
    val bitrate: Int = 128
) {
    var genre: Genre? = null
}

data class Genre(
    val id: Int,
    val name: String,
    val hasSubGenres: Boolean
) {
    var parent: Genre? = null
    var subGenres: MutableList<Genre>? = null
    var stations: MutableList<Station>? = null
    var areAllStationsLoaded: Boolean = false
}

fun stationFromResponse(resp: StationResponse): Station {
    return Station(
        id = resp.id,
        name = resp.name,
        listeners = resp.numberListeners,
        bitrate = resp.bitRate
    )
}

fun genreFromResponse(resp: GenreResponse): Genre {
    return Genre(
        id = resp.id,
        name = resp.name,
        hasSubGenres = resp.hasChildren
    )
}
