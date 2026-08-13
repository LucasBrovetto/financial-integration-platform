# Simplified ISO 8583 Authorization Profile

## Purpose

This document explains how `transaction-service` and `acquirer-simulator`
exchange authorization messages. Both applications need to follow the same
rules so that they can understand each other over a TCP/IP connection.

The profile covers only the information needed to demonstrate a sale. It does
not include card numbers or any other cardholder data. All examples use made-up
values and are safe to keep in the repository.

The first Sprint 2 increment lets the simulator read an authorization request
and build a response. A later increment will connect `transaction-service` as
the TCP client.

The project uses jPOS `2.1.10` because it works with the Java 21 runtime already
used by the platform.

## Transport

| Property | Value |
|---|---|
| Protocol | TCP/IP |
| Channel | jPOS `ASCIIChannel` |
| Message length | Four ASCII digits added before each message |
| ISO version | Simplified ISO 8583:1987 profile |
| Field encoding | ASCII |
| Bitmap | Binary |
| Connection model | Keep the connection open for more than one request |
| Request timeout | 5 seconds |

The simulator handles each TCP connection with a Java 21 virtual thread. The
operator console remains sequential so that prompts from concurrent requests do
not become mixed together.

Before sending an ISO 8583 message, `ASCIIChannel` adds four characters that
tell the receiver how many bytes it must read. For example, a 120-byte message
starts with `0120`. A client that omits this prefix cannot communicate with the
simulator.

## Authorization Request (`0200`)

`transaction-service` sends a message with MTI `0200` when it wants the
acquirer to approve or reject a sale.

| Field | Name | Format | Required | Example |
|---:|---|---|---|---|
| 0 | Message type indicator | `n4` | Yes | `0200` |
| 3 | Processing code | `n6` | Yes | `000000` |
| 4 | Amount in minor units | `n12` | Yes | `000000015050` |
| 7 | Transmission date and time | `n10` (`MMddHHmmss`) | Yes | `0804134500` |
| 11 | Systems trace audit number | `n6` | Yes | `000001` |
| 37 | Retrieval reference number | `an12` | Yes | `626216000001` |
| 41 | Terminal identifier | `ans8` | Yes | `TERM0001` |
| 49 | ISO 4217 numeric currency code | `n3` | Yes | `858` |

In this project, processing code `000000` means that the operation is a sale.

The amount does not include a decimal separator. It is sent in the smallest
unit of the currency, such as cents. An amount of `150.50` is therefore sent as
`000000015050`.

The STAN is a short number used to follow a request while it is being processed.
The RRN is a longer reference kept with the transaction so that the operation
can be identified later.

## Authorization Response (`0210`)

After processing the request, the simulator returns MTI `0210`. It copies the
operation details from the original request and adds field 39, which tells the
client whether the sale was approved or rejected.

| Field | Name | Format | Required | Example |
|---:|---|---|---|---|
| 0 | Message type indicator | `n4` | Yes | `0210` |
| 39 | Response code | `an2` | Yes | `00` |

## How the Operator Chooses a Result

When a request arrives, the simulator shows its amount, STAN, RRN, terminal,
and currency in the console. It does not send a response until the operator
chooses an option and confirms it.

| Console option | Result shown by the simulator | Field 39 |
|---:|---|---:|
| 1 | The sale is approved | `00` |
| 2 | The bank rejected the sale without providing a specific reason | `05` |
| 3 | The sale is rejected because there are not enough funds | `51` |
| 4 | The acquirer could not process the sale because of an internal system error | `96` |
| 5 | The simulator does not answer, allowing the client timeout to be tested | No response |

If the operator rejects the first selection at the confirmation prompt, the
simulator displays the menu again. This makes it possible to inspect every
request and deliberately demonstrate each authorization outcome.

When the simulator does not answer, the result is unknown. It must not be
treated as a rejection because the acquirer may have processed the request even
though the response did not reach `transaction-service`. The client must not
automatically send the same financial request again.

## Correlation

The client must make sure that every `0210` belongs to the correct `0200`.
It does this by comparing the STAN in field 11 and the terminal identifier in
field 41. The RRN in field 37 must also be the same and is stored as the lasting
reference for the operation.

## Data Protection Boundary

This profile does not send the card number (PAN), cardholder name, expiry date,
CVV, magnetic-stripe data, PIN blocks, or EMV data. Tests and demonstrations
must always use synthetic transaction information.
