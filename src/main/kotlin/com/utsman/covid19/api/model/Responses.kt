package com.utsman.covid19.api.model

data class Responses(
        val message: String,
        val total: Total,
        val data: List<Data>?,
        val sources: List<Sources>,
        val author: String)

data class ResponsesCountry(
        val message: String,
        val total: Total,
        val countries: List<DataCountry>?,
        val sources: List<Sources>,
        val author: String)