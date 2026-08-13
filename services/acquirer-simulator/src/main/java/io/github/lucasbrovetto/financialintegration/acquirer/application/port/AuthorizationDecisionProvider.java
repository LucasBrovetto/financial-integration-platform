package io.github.lucasbrovetto.financialintegration.acquirer.application.port;

import io.github.lucasbrovetto.financialintegration.acquirer.domain.AuthorizationDecision;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import java.io.IOException;

@FunctionalInterface
public interface AuthorizationDecisionProvider {

    AuthorizationDecision decide(ISOMsg request) throws IOException, ISOException;
}
