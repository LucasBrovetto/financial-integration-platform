package io.github.lucasbrovetto.financialintegration.acquirer.adapter.console;

import io.github.lucasbrovetto.financialintegration.acquirer.application.port.AuthorizationDecisionProvider;
import io.github.lucasbrovetto.financialintegration.acquirer.domain.AuthorizationDecision;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ConsoleAuthorizationDecisionProvider implements AuthorizationDecisionProvider {

    private final BufferedReader input;
    private final PrintStream output;
    private final Lock consoleLock = new ReentrantLock(true);

    public ConsoleAuthorizationDecisionProvider() {
        this(System.in, System.out);
    }

    ConsoleAuthorizationDecisionProvider(InputStream input, PrintStream output) {
        this.input = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        this.output = output;
    }

    @Override
    public AuthorizationDecision decide(ISOMsg request) throws IOException, ISOException {
        consoleLock.lock();
        try {
            printRequest(request);

            while (true) {
                printMenu();
                AuthorizationDecision decision = readDecision();
                output.printf("Selected response: %s%n", decision.getDescription());
                output.print("Confirm this response? [y/n]: ");

                if ("y".equalsIgnoreCase(readRequiredLine().trim())) {
                    return decision;
                }

                output.println("Selection cancelled. Choose another response.");
            }
        } finally {
            consoleLock.unlock();
        }
    }

    private void printRequest(ISOMsg request) throws ISOException {
        output.println();
        output.println("============================================================");
        output.println("New ISO 8583 authorization request");
        output.println("============================================================");
        output.printf("MTI:             %s%n", request.getMTI());
        output.printf("Amount:          %s%n", formatAmount(request.getString(4)));
        output.printf("STAN:            %s%n", request.getString(11));
        output.printf("RRN:             %s%n", request.getString(37));
        output.printf("Terminal:        %s%n", request.getString(41));
        output.printf("Currency code:   %s%n", request.getString(49));
        output.println();
    }

    private void printMenu() {
        output.println("Choose the response to send:");
        for (AuthorizationDecision decision : AuthorizationDecision.values()) {
            String field39 = decision.sendsResponse() ? "field 39=" + decision.getResponseCode() : "no response";
            output.printf("  %s. %s (%s)%n", decision.getOption(), decision.getDescription(), field39);
        }
        output.print("Option: ");
    }

    private AuthorizationDecision readDecision() throws IOException {
        while (true) {
            String option = readRequiredLine().trim();
            var decision = AuthorizationDecision.fromOption(option);
            if (decision.isPresent()) {
                return decision.get();
            }
            output.print("Invalid option. Enter a number from 1 to 5: ");
        }
    }

    private String readRequiredLine() throws IOException {
        String line = input.readLine();
        if (line == null) {
            throw new IOException("Console input was closed while an authorization was waiting for a decision");
        }
        return line;
    }

    private String formatAmount(String amount) {
        if (amount == null || !amount.matches("\\d{1,12}")) {
            return String.valueOf(amount);
        }
        return new BigDecimal(amount).movePointLeft(2).toPlainString();
    }
}
