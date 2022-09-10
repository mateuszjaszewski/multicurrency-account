package pl.mj.multicurrencyaccount.application

import pl.mj.multicurrencyaccount.shared.Currency
import java.math.BigDecimal

interface CurrencyRatioProvider {
    fun ratio(source: Currency, destination: Currency) : BigDecimal
}