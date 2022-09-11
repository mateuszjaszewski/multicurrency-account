package pl.mj.multicurrencyaccount.application

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
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
    @Operation(summary = "Registers a new account")
    fun registerAccount(@RequestBody request: RegisterAccountRequest) {
        accountFacade.registerAccount(request)
    }

    @PostMapping("/{pesel}/transactions/currency-exchanges")
    @Operation(summary = "Performs money exchange form one currency to other")
    fun exchangeMoney(@PathVariable @Schema(example = "64102278587") pesel: String,
                      @RequestBody request: MoneyExchangeRequest) {
        accountFacade.exchangeMoney(pesel, request)
    }

    @GetMapping("/{pesel}")
    @Operation(summary = "Returns details about account - owner data and current balance")
    fun accountDetails(@PathVariable @Schema(example = "64102278587") pesel: String): AccountDetailsResponse {
        return accountFacade.accountDetails(pesel)
    }

    @GetMapping("/{pesel}/transactions")
    @Operation(summary = "Returns all transactions performed on account")
    fun accountTransactions(@PathVariable @Schema(example = "64102278587") pesel: String): AccountTransactionsResponse {
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
    fun handleNotFoundException() {
    }

    class RegisterAccountRequest(val owner: OwnerDto,
                                 @field:Schema(example = "1000.00") val initialDeposit: BigDecimal)

    class MoneyExchangeRequest(@field:Schema(example = "100.00") val amount: BigDecimal,
                               @field:Schema(example = "PLN") val sourceCurrency: Currency,
                               @field:Schema(example = "USD") val targetCurrency: Currency)

    class AccountDetailsResponse(val owner: OwnerDto, val subAccounts: List<SubAccountDto>)
    class AccountTransactionsResponse(val transactions: List<TransactionDto>)
    class ErrorResponse(val message: String)

    class OwnerDto(@field:Schema(example = "64102278587") val pesel: String,
                   @field:Schema(example = "Jan") val firstName: String,
                   @field:Schema(example = "Kowalski") val lastName: String)

    class SubAccountDto(val currency: Currency, val balance: BigDecimal)

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(
            JsonSubTypes.Type(value = CurrencyBoughtTransactionDto::class, name = "CURRENCY_BOUGHT"),
            JsonSubTypes.Type(value = CurrencySoldTransactionDto::class, name = "CURRENCY_SOLD"),
            JsonSubTypes.Type(value = InitialDepositTransactionDto::class, name = "INITIAL_DEPOSIT")
    )
    sealed class TransactionDto(val timestamp: Instant)

    class CurrencyBoughtTransactionDto(timestamp: Instant, val currency: Currency, val amount: BigDecimal, val rate: BigDecimal)
        : TransactionDto(timestamp)

    class CurrencySoldTransactionDto(timestamp: Instant, val currency: Currency, val amount: BigDecimal, val rate: BigDecimal)
        : TransactionDto(timestamp)

    class InitialDepositTransactionDto(timestamp: Instant, val initialDeposit: BigDecimal)
        : TransactionDto(timestamp)
}