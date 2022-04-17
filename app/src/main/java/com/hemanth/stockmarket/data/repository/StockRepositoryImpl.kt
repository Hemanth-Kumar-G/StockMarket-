package com.hemanth.stockmarket.data.repository

import com.hemanth.stockmarket.data.csv.CSVParser
import com.hemanth.stockmarket.data.local.StockDatabase
import com.hemanth.stockmarket.data.mapper.toCompanyListing
import com.hemanth.stockmarket.data.mapper.toCompanyListingEntity
import com.hemanth.stockmarket.data.remote.StockApi
import com.hemanth.stockmarket.domain.model.CompanyListing
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
    private val companyListingsParser: CSVParser<CompanyListing>
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
}