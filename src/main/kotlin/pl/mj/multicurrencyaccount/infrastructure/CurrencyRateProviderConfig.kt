package pl.mj.multicurrencyaccount.infrastructure

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class CurrencyRateProviderConfig {

    @Bean
    fun nbpApiRestTemplate() : RestTemplate {
        return RestTemplateBuilder()
                .rootUri("https://api.nbp.pl/api/exchangerates/rates/c/")
                //.defaultHeader("Accept", "application/json")
                .build()
    }

}