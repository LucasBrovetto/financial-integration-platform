package io.github.lucasbrovetto.financialintegration.acquirer.adapter.tcp;

import io.github.lucasbrovetto.financialintegration.acquirer.adapter.console.ConsoleAuthorizationDecisionProvider;
import io.github.lucasbrovetto.financialintegration.acquirer.application.exception.AcquirerProcessingException;
import io.github.lucasbrovetto.financialintegration.acquirer.application.port.AuthorizationDecisionProvider;
import io.github.lucasbrovetto.financialintegration.acquirer.domain.AuthorizationDecision;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;

import java.io.IOException;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Slf4j
public final class AuthorizationRequestListener implements ISORequestListener {

    private static final String AUTHORIZATION_REQUEST_MTI = "0200";
    private final AuthorizationDecisionProvider decisionProvider;

    public AuthorizationRequestListener() {
        this(new ConsoleAuthorizationDecisionProvider());
    }

    @Override
    public boolean process(ISOSource source, ISOMsg request) {
        try {
            if (!AUTHORIZATION_REQUEST_MTI.equals(request.getMTI())) {
                log.debug("Ignoring unsupported ISO 8583 message with MTI {}", request.getMTI());
                return false;
            }

            log.info(
                    "Authorization request received: stan={}, rrn={}, terminal={}, amount={}, currency={}",
                    request.getString(11),
                    request.getString(37),
                    request.getString(41),
                    request.getString(4),
                    request.getString(49));

            AuthorizationDecision decision = decisionProvider.decide(request);
            if (!decision.sendsResponse()) {
                log.info(
                        "Operator selected timeout: stan={}, rrn={}",
                        request.getString(11),
                        request.getString(37));
                return true;
            }

            ISOMsg response = (ISOMsg) request.clone();
            response.setResponseMTI();
            response.set(39, decision.getResponseCode());
            source.send(response);
            log.info(
                    "Authorization response sent: stan={}, rrn={}, responseCode={}, decision={}",
                    request.getString(11),
                    request.getString(37),
                    decision.getResponseCode(),
                    decision);
            return true;
        } catch (ISOException | IOException exception) {
            throw new AcquirerProcessingException("Unable to process ISO 8583 authorization request", exception);
        }
    }
}
