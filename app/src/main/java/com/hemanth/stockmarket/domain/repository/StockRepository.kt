package com.hemanth.stockmarket.domain.repository

import com.hemanth.stockmarket.domain.model.CompanyListing
import com.hemanth.stockmarket.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListings(
        fetchFromRemote :Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>
}