package io.github.lucasbrovetto.financialintegration.acquirer.testclient;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.GenericPackager;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ManualAuthorizationClient {

    private static final String PACKAGER_RESOURCE = "cfg/packager/acquirer.xml";
    private static final DateTimeFormatter TRANSMISSION_DATE_TIME = DateTimeFormatter.ofPattern("MMddHHmmss");
    private static final int DEFAULT_TIMEOUT_SECONDS = 15;

    public static void main(String[] args) throws Exception {
        BigDecimal amount = args.length > 0 ? new BigDecimal(args[0]) : new BigDecimal("100.00");
        int timeoutSeconds = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_TIMEOUT_SECONDS;
        ISOPackager packager = loadPackager();
        ISOMsg request = createRequest(packager, amount);

        ASCIIChannel channel = new ASCIIChannel("127.0.0.1", 9000, packager);
        channel.setLengthDigits(4);

        try {
            channel.connect();
            channel.setTimeout(timeoutSeconds * 1_000);
            printRequest(request, timeoutSeconds);
            channel.send(request);

            ISOMsg response = channel.receive();
            printResponse(response);
        } catch (SocketTimeoutException exception) {
            System.out.println();
            System.out.println("No response was received before the timeout.");
            System.out.println("Authorization result: UNKNOWN");
        } finally {
            if (channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    private static ISOMsg createRequest(ISOPackager packager, BigDecimal amount) throws Exception {
        long trace = System.currentTimeMillis() % 1_000_000;
        String stan = "%06d".formatted(trace);

        ISOMsg request = new ISOMsg();
        request.setPackager(packager);
        request.setMTI("0200");
        request.set(3, "000000");
        request.set(4, formatIsoAmount(amount));
        request.set(7, LocalDateTime.now().format(TRANSMISSION_DATE_TIME));
        request.set(11, stan);
        request.set(37, "626216" + stan);
        request.set(41, "TERM0001");
        request.set(49, "858");
        return request;
    }

    private static String formatIsoAmount(BigDecimal amount) {
        long minorUnits = amount
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact();
        if (minorUnits <= 0 || minorUnits > 999_999_999_999L) {
            throw new IllegalArgumentException("Amount must be positive and fit in ISO field 4");
        }
        return "%012d".formatted(minorUnits);
    }

    private static ISOPackager loadPackager() throws Exception {
        InputStream configuration = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(PACKAGER_RESOURCE);
        if (configuration == null) {
            throw new IllegalStateException("Packager configuration not found: " + PACKAGER_RESOURCE);
        }
        return new GenericPackager(configuration);
    }

    private static void printRequest(ISOMsg request, int timeoutSeconds) throws Exception {
        System.out.println("Sending ISO 8583 authorization request:");
        System.out.printf("  MTI:      %s%n", request.getMTI());
        System.out.printf("  Amount:   %s%n", request.getString(4));
        System.out.printf("  STAN:     %s%n", request.getString(11));
        System.out.printf("  RRN:      %s%n", request.getString(37));
        System.out.printf("  Terminal: %s%n", request.getString(41));
        System.out.printf("Waiting up to %d seconds for the operator response...%n", timeoutSeconds);
    }

    private static void printResponse(ISOMsg response) throws Exception {
        System.out.println();
        System.out.println("ISO 8583 authorization response received:");
        System.out.printf("  MTI:      %s%n", response.getMTI());
        System.out.printf("  Field 39: %s%n", response.getString(39));
        System.out.printf("  STAN:     %s%n", response.getString(11));
        System.out.printf("  RRN:      %s%n", response.getString(37));
    }
}
