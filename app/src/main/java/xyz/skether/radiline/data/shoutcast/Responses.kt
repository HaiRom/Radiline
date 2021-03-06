package xyz.skether.radiline.data.shoutcast

import com.github.kittinunf.fuel.core.ResponseDeserializable
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

data class StationListResponse(
    val tuneInList: List<TuneInResponse>,
    val stations: List<StationResponse>
) {

    object LegacyDeserializer : ResponseDeserializable<StationListResponse> {
        override fun deserialize(inputStream: InputStream): StationListResponse {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
            val rootElement = doc.firstChild as Element
            return stationListFromElement(rootElement)
        }
    }

    object Deserializer : ResponseDeserializable<StationListResponse> {
        override fun deserialize(inputStream: InputStream): StationListResponse {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
            val root = doc.firstChild as Element
            val dataElement = root.getElementsByTagName("data").item(0) as Element
            val stationListElement = dataElement.getElementsByTagName("stationlist").item(0) as Element
            return stationListFromElement(stationListElement)
        }
    }

}

private fun stationListFromElement(dataElement: Element): StationListResponse {
    val tuneInList = mutableListOf<TuneInResponse>()
    val tuneInAttrs = dataElement.getElementsByTagName("tunein").item(0).attributes
    for (i in 0 until tuneInAttrs.length) {
        tuneInList.add(tuneInFromNode(tuneInAttrs.item(i)))
    }

    val stations = mutableListOf<StationResponse>()
    val stationNodes = dataElement.getElementsByTagName("station")
    for (i in 0 until stationNodes.length) {
        stations.add(stationFromNode(stationNodes.item(i)))
    }

    return StationListResponse(tuneInList, stations)
}

data class TuneInResponse(
    val base: String,
    val resource: String
)

private fun tuneInFromNode(node: Node) = TuneInResponse(
    base = node.nodeName,
    resource = node.nodeValue
)

data class StationResponse(
    val id: Long,
    val name: String,
    val genreName: String?,
    val mediaType: String,
    val bitRate: Int,
    val currentTrack: String?,
    val numberListeners: Int,
    val logo: String?
)

private fun stationFromNode(node: Node): StationResponse {
    val attrs = node.attributes
    return StationResponse(
        id = attrs.nodeValue("id").toLong(),
        name = attrs.nodeValue("name"),
        genreName = attrs.nodeValueOpt("genre"),
        mediaType = attrs.nodeValue("mt"),
        bitRate = attrs.nodeValue("br").toInt(),
        currentTrack = attrs.nodeValueOpt("ct"),
        numberListeners = attrs.nodeValue("lc").toInt(),
        logo = attrs.nodeValueOpt("logo")
    )
}

data class GenreListResponse(
    val genres: List<GenreResponse>
) {

    object Deserializer : ResponseDeserializable<GenreListResponse> {
        override fun deserialize(inputStream: InputStream): GenreListResponse {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
            val root = doc.firstChild as Element
            val dataElement = root.getElementsByTagName("data").item(0) as Element
            val genreListElement = dataElement.getElementsByTagName("genrelist").item(0) as Element

            val genres = mutableListOf<GenreResponse>()
            val genreNodes = genreListElement.getElementsByTagName("genre")
            for (i in 0 until genreNodes.length) {
                genres.add(genreFromNode(genreNodes.item(i)))
            }

            return GenreListResponse(genres)
        }
    }

}

data class GenreResponse(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val hasChildren: Boolean
)

private fun genreFromNode(node: Node): GenreResponse {
    val attrs = node.attributes
    var parentId = attrs.nodeValueOpt("parentid")?.toLong()
    if (parentId != null && parentId == 0L) {
        parentId = null
    }
    return GenreResponse(
        id = attrs.nodeValue("id").toLong(),
        parentId = parentId,
        name = attrs.nodeValue("name"),
        hasChildren = attrs.nodeValue("haschildren").toBoolean()
    )
}

data class PlaylistResponse(
    val trackList: List<TrackResponse>
) {

    object Deserializer : ResponseDeserializable<PlaylistResponse> {
        override fun deserialize(inputStream: InputStream): PlaylistResponse? {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
            val root = doc.firstChild as Element
            val trackListElement = root.getElementsByTagName("trackList").item(0) as Element

            val trackList = mutableListOf<TrackResponse>()
            val trackNodes = trackListElement.getElementsByTagName("track")
            for (i in 0 until trackNodes.length) {
                val element = trackNodes.item(i) as Element
                trackList.add(trackFromElement(element))
            }

            return PlaylistResponse(trackList)
        }
    }

}

data class TrackResponse(
    val title: String,
    val location: String
)

private fun trackFromElement(element: Element) = TrackResponse(
    title = element.getElementsByTagName("title").item(0).textContent,
    location = element.getElementsByTagName("location").item(0).textContent
)

private fun NamedNodeMap.nodeValue(name: String): String = getNamedItem(name).nodeValue

private fun NamedNodeMap.nodeValueOpt(name: String): String? = getNamedItem(name)?.nodeValue