package pl.mj.multicurrencyaccount.application

import pl.mj.multicurrencyaccount.domain.Currency
import java.math.BigDecimal

interface CurrencyRateProvider {
    fun buyingRate(currency: Currency) : BigDecimal
    fun sellingRate(currency: Currency) : BigDecimal
}