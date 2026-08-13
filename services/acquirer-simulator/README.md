# Acquirer Simulator

This module receives realistic ISO 8583 authorization requests over TCP/IP and
lets an operator choose the response from the console. It has no graphical user
interface.

## Run from IntelliJ IDEA

1. Open `AcquirerSimulatorApplication`.
2. Run its `main` method.
3. Wait until the console reports that TCP port `9000` is ready.
4. Send an ISO 8583 `0200` request from a compatible client.
5. Review the request, choose a response, and confirm it in the console.

Set the `ACQUIRER_PORT` environment variable when a port other than `9000` is
required.

The TCP acceptor delegates every client connection to a Java 21 virtual thread.
This allows many connections to wait for network data or an operator decision
without reserving one operating-system thread per client. The code intentionally
uses straightforward blocking I/O instead of adding a reactive framework.

## Operator Responses

| Option | Result | Field 39 |
|---:|---|---:|
| 1 | Approve the sale | `00` |
| 2 | Reject the sale without a specific reason | `05` |
| 3 | Reject the sale because of insufficient funds | `51` |
| 4 | Return an acquirer system error | `96` |
| 5 | Send no response and let the client timeout | None |

Only one request can be handled through the console at a time. If more requests
arrive while the operator is deciding, they wait until the current selection is
confirmed. The network connections remain independent and are parked on virtual
threads while they wait.

## Manual Test Without Transaction Service

The test sources include `ManualAuthorizationClient`, a small jPOS client that
sends a real `0200` message directly to the simulator.

1. Run `AcquirerSimulatorApplication` and leave its console open.
2. Run `ManualAuthorizationClient` in a second IntelliJ run configuration.
3. Choose and confirm the response in the simulator console.
4. Inspect the `0210` response in the client console.

The client sends `100.00` and waits 15 seconds by default. IntelliJ program
arguments can override both values:

```text
250.75 30
```

The first argument is the amount and the second is the response timeout in
seconds. Select option 5 in the simulator to confirm that the client finishes
with an `UNKNOWN` result after its timeout.

## Logs

Technical events are written through SLF4J and Logback to both the console and:

```text
logs/acquirer-simulator.log
```

Archived logs are compressed daily, retained for 14 days, and capped at 200 MB
in total. Set `LOG_DIR` to store them in another directory. The interactive
operator menu continues to use standard console input and output because it is
part of the simulator interface rather than an application log.
