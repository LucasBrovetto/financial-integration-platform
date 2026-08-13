package io.github.lucasbrovetto.financialintegration.acquirer.adapter.tcp;

import io.github.lucasbrovetto.financialintegration.acquirer.domain.AuthorizationDecision;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationRequestListenerTest {

    @Test
    void returnsApprovedFinancialResponse() throws Exception {
        AuthorizationRequestListener listener = listenerReturning(AuthorizationDecision.APPROVED);
        CapturingIsoSource source = new CapturingIsoSource();
        ISOMsg request = authorizationRequest();

        assertTrue(listener.process(source, request));
        assertEquals("0210", source.response().getMTI());
        assertEquals("00", source.response().getString(39));
        assertEquals("123456", source.response().getString(11));
        assertEquals("626216123456", source.response().getString(37));
    }

    @Test
    void returnsDeclinedFinancialResponse() throws Exception {
        AuthorizationRequestListener listener = listenerReturning(AuthorizationDecision.INSUFFICIENT_FUNDS);
        CapturingIsoSource source = new CapturingIsoSource();

        assertTrue(listener.process(source, authorizationRequest()));
        assertEquals("0210", source.response().getMTI());
        assertEquals("51", source.response().getString(39));
    }

    @Test
    void deliberatelyDoesNotRespondToTimeoutScenario() throws Exception {
        AuthorizationRequestListener listener = listenerReturning(AuthorizationDecision.TIMEOUT);
        CapturingIsoSource source = new CapturingIsoSource();

        assertTrue(listener.process(source, authorizationRequest()));
        assertNull(source.response());
    }

    @Test
    void leavesUnsupportedMessageForAnotherListener() throws Exception {
        AuthorizationRequestListener listener = listenerReturning(AuthorizationDecision.APPROVED);
        CapturingIsoSource source = new CapturingIsoSource();
        ISOMsg request = authorizationRequest();
        request.setMTI("0800");

        assertFalse(listener.process(source, request));
        assertNull(source.response());
    }

    private AuthorizationRequestListener listenerReturning(AuthorizationDecision decision) {
        return new AuthorizationRequestListener(request -> decision);
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

    private static final class CapturingIsoSource implements ISOSource {

        private final AtomicReference<ISOMsg> response = new AtomicReference<>();

        @Override
        public void send(ISOMsg message) {
            response.set(message);
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        ISOMsg response() {
            return response.get();
        }
    }
}
