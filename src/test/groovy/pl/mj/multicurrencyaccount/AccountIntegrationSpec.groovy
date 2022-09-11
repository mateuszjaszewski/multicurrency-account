package pl.mj.multicurrencyaccount

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import pl.mj.multicurrencyaccount.application.CurrencyRateProvider
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static pl.mj.multicurrencyaccount.application.AccountController.*
import static pl.mj.multicurrencyaccount.domain.Currency.PLN
import static pl.mj.multicurrencyaccount.domain.Currency.USD

@Stepwise
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class AccountIntegrationSpec extends Specification {

    private static final String PESEl = "88010181896"
    private static final String FIRST_NAME = "JAN"
    private static final String LAST_NAME = "KOWALSKI"

    private static final BigDecimal INITIAL_DEPOSIT = 1000.00
    private static final BigDecimal BUY_USD_AMOUNT = 100.00
    private static final BigDecimal SELL_USD_AMOUNT = 50.00

    private static final BigDecimal BUY_USD_RATE = 4.00
    private static final BigDecimal SELL_USD_RATE = 3.50

    @Autowired
    private MockMvc mockMvc

    @SpringBean
    private CurrencyRateProvider currencyRateProvider = Mock()

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new KotlinModule())
            .registerModule(new JavaTimeModule())

    def setup() {
        currencyRateProvider.buyingRate(USD) >> BUY_USD_RATE
        currencyRateProvider.sellingRate(USD) >> SELL_USD_RATE
    }

    def 'should register account'() {
        given:
        def request = new RegisterAccountRequest(new OwnerDto(PESEl, FIRST_NAME, LAST_NAME), INITIAL_DEPOSIT)

        when:
        def response = mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(request)))
                .andReturn().response

        then:
        response.status == HttpStatus.OK.value()
    }

    def 'should exchange some PLN to USD'() {
        given:
        def request = new MoneyExchangeRequest(BUY_USD_AMOUNT, PLN, USD)

        when:
        def response = mockMvc.perform(post("/api/accounts/$PESEl/transactions/currency-exchanges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(request)))
                .andReturn().response

        then:
        response.status == HttpStatus.OK.value()
    }

    def 'should exchange some USD to PLN'() {
        given:
        def request = new MoneyExchangeRequest(SELL_USD_AMOUNT, USD, PLN)

        when:
        def response = mockMvc.perform(post("/api/accounts/$PESEl/transactions/currency-exchanges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(request)))
                .andReturn().response

        then:
        response.status == HttpStatus.OK.value()
    }

    def 'should return account details'() {
        when:
        def response = mockMvc.perform(get("/api/accounts/$PESEl"))
                .andReturn().response

        then:
        response.status == HttpStatus.OK.value()

        and:
        def accountDetails = parseJson(response.getContentAsString(), AccountDetailsResponse.class)
        accountDetails.owner.pesel == PESEl
        accountDetails.owner.firstName == FIRST_NAME
        accountDetails.owner.lastName == LAST_NAME
        accountDetails.subAccounts.find {it.currency == PLN}.balance == 775.00
        accountDetails.subAccounts.find {it.currency == USD}.balance == 50.00
    }

    def 'should return account transactions'() {
        when:
        def response = mockMvc.perform(get("/api/accounts/$PESEl/transactions"))
                .andReturn().response

        then:
        response.status == HttpStatus.OK.value()

        and:
        def accountTransactions = parseJson(response.getContentAsString(), AccountTransactionsResponse.class)
        def transactions = accountTransactions.transactions

        and:
        def currencySoldTransaction = (transactions[0] as CurrencySoldTransactionDto)
        currencySoldTransaction.amount == SELL_USD_AMOUNT
        currencySoldTransaction.rate == SELL_USD_RATE
        currencySoldTransaction.currency == USD

        and:
        def currencyBoughtTransaction = (transactions[1] as CurrencyBoughtTransactionDto)
        currencyBoughtTransaction.amount == BUY_USD_AMOUNT
        currencyBoughtTransaction.rate == BUY_USD_RATE
        currencyBoughtTransaction.currency == USD

        and:
        def initialDepositTransaction = (transactions[2] as InitialDepositTransactionDto)
        initialDepositTransaction.initialDeposit == INITIAL_DEPOSIT
    }

    def 'should respond with status BAD_REQUEST when there is not enough money on account'() {
        given:
        def request = new MoneyExchangeRequest(1_000_000.00, PLN, USD)

        when:
        def response = mockMvc.perform(post("/api/accounts/$PESEl/transactions/currency-exchanges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(request)))
                .andReturn().response

        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        parseJson(response.getContentAsString(), ErrorResponse).message.contains("Insufficient founds")
    }

    def 'should respond with status NOT_FOUND when there is no registered account for given PESEL'() {
        given:
        def peselWithoutRegisteredAccount = "84081564691"

        when:
        def response = mockMvc.perform(get("/api/accounts/$peselWithoutRegisteredAccount"))
                .andReturn().response

        then:
        response.status == HttpStatus.NOT_FOUND.value()
    }

    private <T> T parseJson(String response, Class<T> aClass) {
        return objectMapper.readValue(response, aClass)
    }

    private String asJson(Object object) {
        return objectMapper.writeValueAsString(object)
    }
}
