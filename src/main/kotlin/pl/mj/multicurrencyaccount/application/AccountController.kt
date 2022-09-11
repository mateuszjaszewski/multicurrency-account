package pl.mj.multicurrencyaccount.application

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
    fun registerAccount(@RequestBody request: CreateAccountRequest) {
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

    class CreateAccountRequest(val owner: OwnerDto, val initialDeposit: BigDecimal)
    class MoneyExchangeRequest(val amount: BigDecimal, val sourceCurrency: Currency, val targetCurrency: Currency)

    class AccountDetailsResponse(val owner: OwnerDto, val subAccounts: List<SubAccountDto>)
    class AccountTransactionsResponse(val transactions: List<TransactionDto>)
    class ErrorResponse(val message: String)

    class OwnerDto(val pesel: String, val firstName: String, val lastName: String)
    class SubAccountDto(val currency: Currency, val balance: BigDecimal)

    sealed class TransactionDto(val timestamp: Instant, val type: Type) {
        enum class Type { CURRENCY_BOUGHT, CURRENCY_SOLD, INITIAL_DEPOSIT }
    }
    class CurrencyBoughtTransactionDto(timestamp: Instant, val currency: Currency, val amount: BigDecimal, val rate: BigDecimal)
        : TransactionDto(timestamp, Type.CURRENCY_BOUGHT)
    class CurrencySoldTransactionDto(timestamp: Instant, val currency: Currency, val amount: BigDecimal, val rate: BigDecimal)
        : TransactionDto(timestamp, Type.CURRENCY_SOLD)
    class InitialDepositTransactionDto(timestamp: Instant, val initialDeposit: BigDecimal)
        : TransactionDto(timestamp, Type.INITIAL_DEPOSIT)
}