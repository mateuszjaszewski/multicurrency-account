package pl.mj.multicurrencyaccount.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
@EnableConfigurationProperties(CurrencyRateProviderConfig.NbpApiProperties::class)
class CurrencyRateProviderConfig {

    @Bean
    fun nbpApiRestTemplate(properties: NbpApiProperties) : RestTemplate {
        return RestTemplateBuilder()
                .rootUri(properties.url)
                .setConnectTimeout(properties.readTimeout)
                .setReadTimeout(properties.readTimeout)
                .build()
    }

    @ConstructorBinding
    @ConfigurationProperties(prefix = "nbp.api")
    data class NbpApiProperties(val url: String, val connectTimeout: Duration, val readTimeout: Duration)

}