package com.hemanth.stockmarket.presentation.companuInfo

import com.hemanth.stockmarket.domain.model.CompanyInfo
import com.hemanth.stockmarket.domain.model.IntradayInfo

data class CompanyInfoState(
    val stockInfos: List<IntradayInfo> = emptyList(),
    val company: CompanyInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
