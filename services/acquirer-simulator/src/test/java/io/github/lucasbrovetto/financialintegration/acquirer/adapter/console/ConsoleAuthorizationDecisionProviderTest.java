package io.github.lucasbrovetto.financialintegration.acquirer.adapter.console;

import io.github.lucasbrovetto.financialintegration.acquirer.domain.AuthorizationDecision;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleAuthorizationDecisionProviderTest {

    @Test
    void displaysRequestAndReturnsConfirmedDecision() throws Exception {
        ByteArrayOutputStream console = new ByteArrayOutputStream();
        ConsoleAuthorizationDecisionProvider provider = providerWithInput("2\ny\n", console);

        AuthorizationDecision decision = provider.decide(authorizationRequest());

        assertEquals(AuthorizationDecision.DO_NOT_HONOR, decision);
        String output = console.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Amount:          100.00"));
        assertTrue(output.contains("STAN:            123456"));
        assertTrue(output.contains("RRN:             626216123456"));
        assertTrue(output.contains("field 39=05"));
    }

    @Test
    void allowsOperatorToCancelAndChooseAgain() throws Exception {
        ByteArrayOutputStream console = new ByteArrayOutputStream();
        ConsoleAuthorizationDecisionProvider provider = providerWithInput("1\nn\n4\ny\n", console);

        AuthorizationDecision decision = provider.decide(authorizationRequest());

        assertEquals(AuthorizationDecision.SYSTEM_MALFUNCTION, decision);
        assertTrue(console.toString(StandardCharsets.UTF_8).contains("Selection cancelled"));
    }

    private ConsoleAuthorizationDecisionProvider providerWithInput(
            String input,
            ByteArrayOutputStream output) {
        return new ConsoleAuthorizationDecisionProvider(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    private ISOMsg authorizationRequest() throws Exception {
        ISOMsg request = new ISOMsg();
        request.setMTI("0200");
        request.set(4, "000000010000");
        request.set(11, "123456");
        request.set(37, "626216123456");
        request.set(41, "TERM0001");
        request.set(49, "858");
        return request;
    }
}
