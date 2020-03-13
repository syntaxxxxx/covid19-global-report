package com.utsman.covid19.api

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.utsman.covid19.api.model.*
import com.utsman.covid19.api.raw_model.RawCountries
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.SocketException
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.*
import javax.net.ssl.SSLHandshakeException


@RestController
@RequestMapping("/")
class CovidController {
    private val restTemplate = RestTemplate()
    private val author = "Restful API by Muhammad Utsman, data provided by Johns Hopkins University Center for Systems Science and Engineering (JHU CSSE)"

    private val sources = listOf(
            Sources("World Health Organization (WHO)", "https://www.who.int/"),
            Sources("DXY.cn. Pneumonia. 2020", "http://3g.dxy.cn/newh5/view/pneumonia"),
            Sources("BNO News", "https://bnonews.com/index.php/2020/02/the-latest-coronavirus-cases/"),
            Sources("National Health Commission of the People’s Republic of China (NHC)", "http://www.nhc.gov.cn/xcs/yqtb/list_gzbd.shtml"),
            Sources("China CDC (CCDC)", "http://weekly.chinacdc.cn/news/TrackingtheEpidemic.htm"),
            Sources("Hong Kong Department of Health", "https://www.chp.gov.hk/en/features/102465.html"),
            Sources("Macau Government", "https://www.ssm.gov.mo/portal/"),
            Sources("Taiwan CDC", "https://sites.google.com/cdc.gov.tw/2019ncov/taiwan?authuser=0"),
            Sources("US CDC", "https://www.cdc.gov/coronavirus/2019-ncov/index.html"),
            Sources("Government of Canada", "https://www.canada.ca/en/public-health/services/diseases/coronavirus.html"),
            Sources("Australia Government Department of Health", "https://www.health.gov.au/news/coronavirus-update-at-a-glance"),
            Sources("European Centre for Disease Prevention and Control (ECDC)", "https://www.ecdc.europa.eu/en/geographical-distribution-2019-ncov-cases"),
            Sources("Ministry of Health Singapore (MOH)", "https://www.moh.gov.sg/covid-19"),
            Sources("Italy Ministry of Health", "http://www.salute.gov.it/nuovocoronavirus")
    )

    @GetMapping("/api")
    fun getAll(@RequestParam("day") day: Int,
               @RequestParam("month") month: Int,
               @RequestParam("year") year: Int,
               @RequestParam("q") country: String?): Responses? {

        var message = "OK"

        val formatter = DecimalFormat("00")
        val dayFormat = formatter.format(day.toLong())
        val monthFormat = formatter.format(month.toLong())

        val url = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/$monthFormat-$dayFormat-$year.csv"

        val listData: MutableList<Data> = mutableListOf()
        val finalListData: MutableList<Data> = mutableListOf()

        try {
            val responsesString = restTemplate.getForObject(url, String::class.java)
            val obj = responsesString?.let { csvReader().readAll(it) }
            println(obj)

            obj?.forEachIndexed { index, list ->
                if (index != 0 && index != obj.size) {
                    try {
                        val lastUpdate = LocalDateTime
                                .parse(list.get(2))
                                .toLocalDate()

                        val cal = Calendar.getInstance().apply {
                            set(lastUpdate.year, lastUpdate.monthValue, lastUpdate.dayOfMonth)
                        }
                        val time = cal.time.time

                        println(lastUpdate)
                        val data = Data(
                                id = index,
                                country = list.get(1),
                                province_or_state = if (list.get(0) == "") "Unknown" else list.get(0),
                                confirmed = list.get(3).toInt(),
                                death = list.get(4).toInt(),
                                recovered = list.get(5).toInt(),
                                lastUpdate = time,
                                coordinate = listOf(list.get(6).toDouble(), list.get(7).toDouble()))
                        listData.add(data)

                    } catch (e: IndexOutOfBoundsException) {
                        message = "Data not yet available"
                    } catch (e: DateTimeParseException) {
                        message = "Data not yet available"
                    }
                }
            }
        } catch (e: HttpClientErrorException) {
            message = "Data not yet available"
        } catch (e: SocketException) {
            message = "Data not yet available"
        } catch (e: SSLHandshakeException) {
            message = "Data not yet available"
        }

        if (country != null) {
            val newFilterData = listData.filter { it.country?.toLowerCase()?.contains(country.toLowerCase()) == true }
            finalListData.addAll(newFilterData)
        } else {
            finalListData.addAll(listData)
        }

        val totalConfirmed = listData.sumBy { it.confirmed ?: 0 }
        val totalDeath = listData.sumBy { it.death ?: 0 }
        val totalRecovered = listData.sumBy { it.recovered ?: 0 }

        return Responses(
                message = message,
                total = Total(
                        confirmed = totalConfirmed,
                        death = totalDeath,
                        recovered = totalRecovered
                ),
                data = finalListData,
                sources = sources,
                author = author
        )
    }

    @GetMapping("/api/country")
    fun getByCountry(@RequestParam("day") day: Int,
                     @RequestParam("month") month: Int,
                     @RequestParam("year") year: Int,
                     @RequestParam("q") country: String?): ResponsesCountry {


        var message = "OK"

        val formatter = DecimalFormat("00")
        val dayFormat = formatter.format(day.toLong())
        val monthFormat = formatter.format(month.toLong())

        val url = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/$monthFormat-$dayFormat-$year.csv"

        val listDataCountries: MutableList<DataCountry> = mutableListOf()
        val listData: MutableList<Data> = mutableListOf()
        val finalListData: MutableList<DataCountry> = mutableListOf()

        try {
            val responsesString = restTemplate.getForObject(url, String::class.java)
            val obj = responsesString?.let { csvReader().readAll(it) }
            println(obj)

            obj?.forEachIndexed { index, list ->
                if (index != 0 && index != obj.size) {
                    try {
                        val lastUpdate = LocalDateTime
                                .parse(list.get(2))
                                .toLocalDate()

                        val cal = Calendar.getInstance().apply {
                            set(lastUpdate.year, lastUpdate.monthValue, lastUpdate.dayOfMonth)
                        }
                        val time = cal.time.time

                        println(lastUpdate)
                        val data = Data(
                                id = index,
                                country = list.get(1),
                                province_or_state = if (list.get(0) == "") "Unknown" else list.get(0),
                                confirmed = list.get(3).toInt(),
                                death = list.get(4).toInt(),
                                recovered = list.get(5).toInt(),
                                lastUpdate = time,
                                coordinate = listOf(list.get(6).toDouble(), list.get(7).toDouble()))
                        listData.add(data)

                    } catch (e: IndexOutOfBoundsException) {
                        message = "Data not yet available"
                    } catch (e: DateTimeParseException) {
                        message = "Data not yet available"
                    }
                }
            }

            val listCountry = listData.groupBy { it.country }
            listDataCountries.addAll(
                    listCountry.map {
                        DataCountry(
                                country = it.key,
                                total = Total(
                                        it.value.sumBy { d -> d.confirmed ?: 0 },
                                        it.value.sumBy { d -> d.death ?: 0 },
                                        it.value.sumBy { d -> d.recovered ?: 0 }),
                                data = it.value)
                    }
            )

        } catch (e: HttpClientErrorException) {
            message = "Data not yet available"
        } catch (e: SocketException) {
            message = "Data not yet available"
        } catch (e: SSLHandshakeException) {
            message = "Data not yet available"
        }

        if (country != null) {
            val newFilterData = listDataCountries.filter { it.country?.toLowerCase()?.contains(country.toLowerCase()) == true }
            finalListData.addAll(newFilterData)
        } else {
            finalListData.addAll(listDataCountries)
        }

        val totalConfirmed = listData.sumBy { it.confirmed ?: 0 }
        val totalDeath = listData.sumBy { it.death ?: 0 }
        val totalRecovered = listData.sumBy { it.recovered ?: 0 }

        return ResponsesCountry(
                message = message,
                total = Total(
                        confirmed = totalConfirmed,
                        death = totalDeath,
                        recovered = totalRecovered
                ),
                countries = finalListData,
                sources = sources,
                author = author
        )
    }

}