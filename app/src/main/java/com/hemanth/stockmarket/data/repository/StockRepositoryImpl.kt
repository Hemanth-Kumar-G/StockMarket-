package com.hemanth.stockmarket.data.repository

import com.hemanth.stockmarket.data.csv.CSVParser
import com.hemanth.stockmarket.data.local.StockDatabase
import com.hemanth.stockmarket.data.mapper.toCompanyInfo
import com.hemanth.stockmarket.data.mapper.toCompanyListing
import com.hemanth.stockmarket.data.mapper.toCompanyListingEntity
import com.hemanth.stockmarket.data.remote.StockApi
import com.hemanth.stockmarket.domain.model.CompanyInfo
import com.hemanth.stockmarket.domain.model.CompanyListing
import com.hemanth.stockmarket.domain.model.IntradayInfo
import com.hemanth.stockmarket.domain.repository.StockRepository
import com.hemanth.stockmarket.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingsParser: CSVParser<CompanyListing>,
    private val intradayInfoParser: CSVParser<IntradayInfo>
) : StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {

            emit(Resource.Loading(true))

            val localListings = db.dao.searchCompanyListing(query = query)
            emit(Resource.Success(
                data = localListings.map { it.toCompanyListing() }
            ))
            val isDbEmpty = localListings.isEmpty() && query.isEmpty()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote

            if (shouldJustLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListings = try {
                val response = api.getListings()
                companyListingsParser.parse(response.byteStream())
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }

            remoteListings?.let {

                dao.clearCompanyListings()
                dao.insertCompanyListings(it.map { it.toCompanyListingEntity() })
                emit(
                    Resource.Success(
                        data = dao.searchCompanyListing("")
                            .map { it.toCompanyListing() })
                )
                emit(Resource.Loading(false))
            }
        }
    }

    override suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> =
        try {

            val response = api.getIntradayInfo(symbol = symbol)
            val result = intradayInfoParser.parse(response.byteStream())
            Resource.Success(result)
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load intraday info"
            )
        } catch (e: HttpException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load intraday info"
            )
        }

    override suspend fun getCompanyInfo(symbol: String): Resource<CompanyInfo> =
        try {
            val result = api.getCompanyInfo(symbol)
            Resource.Success(result.toCompanyInfo())
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load company info"
            )
        } catch (e: HttpException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load company info"
            )
        }
}
