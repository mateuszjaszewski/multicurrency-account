package pl.mj.multicurrencyaccount.infrastructure

import org.springframework.stereotype.Component
import pl.mj.multicurrencyaccount.shared.Currency
import pl.mj.multicurrencyaccount.application.CurrencyRatioProvider
import java.math.BigDecimal

@Component
class CurrencyRatioProviderImpl : CurrencyRatioProvider {

    override fun ratio(source: Currency, destination: Currency): BigDecimal {
        return BigDecimal.ONE
    }
    
}