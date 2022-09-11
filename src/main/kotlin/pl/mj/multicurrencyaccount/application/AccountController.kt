package pl.mj.multicurrencyaccount.application

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import pl.mj.multicurrencyaccount.application.AccountFacade.*
import pl.mj.multicurrencyaccount.domain.Currency
import pl.mj.multicurrencyaccount.domain.DomainException
import java.math.BigDecimal
import java.time.Instant

@RestController
@RequestMapping("/api/accounts")
class AccountController(val accountFacade: AccountFacade) {

    @PostMapping
    fun registerAccount(@RequestBody request: RegisterAccountRequest) {
        accountFacade.registerAccount(request)
    }

    @PostMapping("/{pesel}/transactions/currency-exchanges")
    fun exchangeMoney(@PathVariable pesel: String,
                      @RequestBody request: MoneyExchangeRequest) {
        accountFacade.exchangeMoney(pesel, request)
    }

    @GetMapping("/{pesel}")
    fun accountDetails(@PathVariable pesel: String): AccountDetailsResponse {
        return accountFacade.accountDetails(pesel)
    }

    @GetMapping("/{pesel}/transactions")
    fun accountTransactions(@PathVariable pesel: String): AccountTransactionsResponse {
        return accountFacade.accountTransactions(pesel)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DomainException::class)
    fun handleDomainException(exception: DomainException): ErrorResponse {
        return ErrorResponse(exception.message)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidOperationException::class)
    fun handleInvalidOperationException(exception: InvalidOperationException): ErrorResponse {
        return ErrorResponse(exception.message)
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AccountNotFoundException::class)
    fun handleNotFoundException() {}

    class RegisterAccountRequest(val owner: OwnerDto, val initialDeposit: BigDecimal)
    class MoneyExchangeRequest(val amount: BigDecimal, val sourceCurrency: Currency, val targetCurrency: Currency)

    class AccountDetailsResponse(val owner: OwnerDto, val subAccounts: List<SubAccountDto>)
    class AccountTransactionsResponse(val transactions: List<TransactionDto>)
    class ErrorResponse(val message: String)

    class OwnerDto(val pesel: String, val firstName: String, val lastName: String)
    class SubAccountDto(val currency: Currency, val balance: BigDecimal)

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(value = CurrencyBoughtDto::class, name = "CURRENCY_BOUGHT"),
        JsonSubTypes.Type(value = CurrencySoldDto::class, name = "CURRENCY_SOLD"),
        JsonSubTypes.Type(value = InitialDepositDto::class, name = "INITIAL_DEPOSIT"),
    )
    sealed class TransactionDto(val timestamp: Instant)
    class CurrencyBoughtDto(timestamp: Instant, val currency: Currency, val amount: BigDecimal, val rate: BigDecimal)
        : TransactionDto(timestamp)
    class CurrencySoldDto(timestamp: Instant, val currency: Currency, val amount: BigDecimal, val rate: BigDecimal)
        : TransactionDto(timestamp)
    class InitialDepositDto(timestamp: Instant, val initialDeposit: BigDecimal)
        : TransactionDto(timestamp)
}