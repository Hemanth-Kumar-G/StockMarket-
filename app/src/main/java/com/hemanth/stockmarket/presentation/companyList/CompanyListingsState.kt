package com.hemanth.stockmarket.presentation.companyList

import com.hemanth.stockmarket.domain.model.CompanyListing

data class CompanyListingsState(
    val companies: List<CompanyListing> = emptyList(),
    val isLoading: Boolean =false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = ""
)
