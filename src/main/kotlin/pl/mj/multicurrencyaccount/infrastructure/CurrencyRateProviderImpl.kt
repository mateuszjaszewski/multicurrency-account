package pl.mj.multicurrencyaccount.infrastructure

import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import pl.mj.multicurrencyaccount.application.CurrencyRateProvider
import pl.mj.multicurrencyaccount.domain.Currency
import java.math.BigDecimal

@Component
class CurrencyRateProviderImpl(private val nbpApiRestTemplate: RestTemplate) : CurrencyRateProvider {

    private class ApiResponse(val rates: List<Rate>)
    private class Rate(val bid: BigDecimal, val ask: BigDecimal)

    override fun buyingRate(currency: Currency): BigDecimal {
        return fetchRate(currency).ask
    }

    override fun sellingRate(currency: Currency): BigDecimal {
        return fetchRate(currency).bid
    }

    private fun fetchRate(currency: Currency): Rate {
        val response = nbpApiRestTemplate.getForEntity("/${currency.name}", ApiResponse::class.java)
        if (!response.statusCode.is2xxSuccessful) {
            throw RuntimeException("Cannot fetch rates from NBP API status ${response.statusCodeValue}")
        }
        return response.body?.rates?.first()
                ?: throw RuntimeException("Empty rates in NBP API response")
    }
}