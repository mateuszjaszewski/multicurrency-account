# Multi-currency account
Aplikacja umożliwia dokonywanie wymiany walut PLN <-> USD po aktualnym kursie pobranym z https://api.nbp.pl/api/exchangerates/rates/c/ .
W ramach zadania zostało przyjęte, że główną walutą jest PLN i w zależności od tego, czy dokonujemy wymiany z czy na złotówki, 
to transakcja traktowana jest jako sprzedaż lub kupno obcej waluty. Wpływa to na kurs, jaki jest przyjmowany dla poszczególnych transakcji.

## Architektura
Aplikacja powstała w języku Kotlin przy wykorzystaniu DDD oraz event-sourcing'u. Ze względu na dość skromną domenę 
takie podejście jest w tym przypadku nadmiarowe, jednak celem było zaprezentowanie jak mogą być wykorzystane wspomniane techniki. 

## Baza danych
Do zapisu zdarzeń wykorzystana jest baza H2, która przechowuje dane w pamięci.

## Wymagania
- Java w wersji co najmniej 1.8
- wolny port 8080

## Uruchomienie
Aplikację można uruchomić poprzez wykonanie polecenia `./gradlew bootRun` lub wywołując metodę main z klasy `MultiCurrencyAccountApp`. 

## Dokumentacja API
Dokumentację API udostępnianego przez aplikację, można znaleźć pod adresem: http://localhost:8080/swagger-ui/index.html#/ .

## Testy
Testy jednostkowe oraz test integracyjny zostały przygotowane z wykorzystaniem języka Groovy i framework'a Spock.
Test integracyjny `AccountIntegrationSpec` został oznaczony jako `@Stepwise` ponieważ powinien być wykonywany w sposób sekwencyjny.
W tym teście najpierw sprawdzana jest pozytywna ścieżka, czyli: 
- rejestracja konta,
- wymiana PLN na USD,
- wymiana USD na PLN,
- pobranie stanu kontakt,
- pobranie listy transakcji wykonanych na koncie.

Następnie wykonywane jest kilka żądań, które kończą się błędami.