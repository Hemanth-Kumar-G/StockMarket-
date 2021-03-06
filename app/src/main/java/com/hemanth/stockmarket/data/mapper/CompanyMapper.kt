package com.hemanth.stockmarket.data.mapper

import com.hemanth.stockmarket.data.local.CompanyListingEntity
import com.hemanth.stockmarket.data.remote.dto.CompanyInfoDto
import com.hemanth.stockmarket.domain.model.CompanyInfo
import com.hemanth.stockmarket.domain.model.CompanyListing

fun CompanyListingEntity.toCompanyListing() = CompanyListing(
    name = name,
    symbol = symbol,
    exchange = exchange
)

fun CompanyListing.toCompanyListingEntity() = CompanyListingEntity(
    name = name,
    symbol = symbol,
    exchange = exchange
)

fun CompanyInfoDto.toCompanyInfo(): CompanyInfo {
    return CompanyInfo(
        symbol = symbol ?: "",
        description = description ?: "",
        name = name ?: "",
        country = country ?: "",
        industry = industry ?: ""
    )
}