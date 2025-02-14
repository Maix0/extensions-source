package eu.kanade.tachiyomi.extension.en.cutiecomics

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.network.interceptor.rateLimitHost
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import java.net.URLDecoder

class EnryuManga : ParsedHttpSource() {

    override val name = "Cutie Comics"

    override val baseUrl = "https://enryumanga.com"

    private val base = baseUrl.toHttpUrl()

    override val lang = "en"

    override val supportsLatest = false

    override val client = network.client.newBuilder()
        .rateLimitHost(baseUrl.toHttpUrl(), 2)
        .build()

    private fun extractProxiedImage(link: String): String {
        val resolved: HttpUrl = base.resolve(link)!!
        val t = resolved.queryParameter("url")!!
        return URLDecoder.decode(t, "UTF-8")
    }

    // ============================== Popular ===============================
    override fun popularMangaRequest(page: Int) = GET("$baseUrl/list", headers)

    override fun popularMangaSelector() = "div.flex.justify-center.items-center.flex-wrap"

    override fun popularMangaFromElement(element: Element) = SManga.create().apply {
        url = element.selectFirst("div > div > a")!!.attributes().get("href")
        title = element.selectFirst("div > div > h2.card-title")!!.ownText()
        thumbnail_url = extractProxiedImage(element.selectFirst("div > div > a > img")!!.attributes().get("src"))
    }

    override fun popularMangaNextPageSelector() = ""

    // =============================== Latest ===============================
    override fun latestUpdatesRequest(page: Int): Request {
        throw UnsupportedOperationException()
    }

    override fun latestUpdatesSelector(): String {
        throw UnsupportedOperationException()
    }

    override fun latestUpdatesFromElement(element: Element): SManga {
        throw UnsupportedOperationException()
    }

    override fun latestUpdatesNextPageSelector(): String? {
        throw UnsupportedOperationException()
    }

    // =============================== Search ===============================
    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        throw UnsupportedOperationException()
        /*
        return if (query.startsWith(PREFIX_SEARCH)) { // URL intent handler
            val id = query.removePrefix(PREFIX_SEARCH)

            client.newCall(GET("$baseUrl/$id"))
                .asObservableSuccess()
                .map(::searchMangaByIdParse)
        } else {
            super.fetchSearchManga(page, query, filters)
        }
         */
    }

    private fun searchMangaByIdParse(response: Response): MangasPage {
        throw UnsupportedOperationException()
        /*val doc = response.asJsoup()
        val details = mangaDetailsParse(doc)
            .apply { setUrlWithoutDomain(doc.location()) }
        return MangasPage(listOf(details), false)
         */
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        throw UnsupportedOperationException()
        /*
        require(query.isNotBlank() && query.length >= 4) { "Invalid search! It should have at least 4 non-blank characters." }
        val body = FormBody.Builder()
            .add("do", "search")
            .add("subaction", "search")
            .add("full_search", "0")
            .add("search_start", "$page")
            .add("result_from", "${(page - 1) * 20 + 1}")
            .add("story", query)
            .build()
        return POST("$baseUrl/index.php?do=search", headers, body)
         */
    }

    override fun searchMangaSelector() = popularMangaSelector()

    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)

    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    // =========================== Manga Details ============================
    override fun mangaDetailsParse(document: Document): SManga {
        var details = document.selectFirst(".hero-content")!!
        return SManga.create().apply {
            title = details.selectFirst("div > h1")!!.ownText()
            description = details.selectFirst("div > p")!!.ownText()
        }
    }

    /*SManga.create().apply {
        status = SManga.COMPLETED
        update_strategy = UpdateStrategy.ONLY_FETCH_ONCE

        title = document.selectFirst("h1#page-title")!!.text()
        thumbnail_url = document.selectFirst("div.galery > img")?.absUrl("src")
        genre = document.select("h3.field-label ~ span").joinToString { it.text() }
    }*/

    // ============================== Chapters ==============================
    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        return client.newCall(GET(manga.url)).asObservableSuccess()
            .map { it: Response ->
                Jsoup.parse(it.toString())
                    .selectFirst("body > div > div.flex.justify-center.flex-wrap")!!.children()
                    .map { elem: Element ->
                        SChapter.create().apply {
                            url = elem.selectFirst("a")!!.attributes().get("href")
                            name = elem.selectFirst(".card-title")!!.ownText()
                            // date = parseDate(elem.selectFirst(".card-body > p").ownText())
                        }
                    }
            }
    }

    override fun chapterListSelector(): String {
        throw UnsupportedOperationException()
    }

    override fun chapterFromElement(element: Element): SChapter {
        throw UnsupportedOperationException()
    }

    // =============================== Pages ================================
    override fun pageListParse(document: Document): List<Page> {
        return document.selectFirst(".flex.flex-col.items-center.mx-auto")!!.children()
            .mapIndexed({ index, img_node ->
                Page(
                    index,
                    imageUrl = extractProxiedImage(img_node.selectFirst("img")!!.attributes().get("src")),
                )
            },)
        /*return document.select("div.galery > img").mapIndexed { index, item ->
            Page(index, imageUrl = item.absUrl("src"))
        }
         */
    }

    override fun imageUrlParse(document: Document): String {
        throw UnsupportedOperationException()
    }
}
