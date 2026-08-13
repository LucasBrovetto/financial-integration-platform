package io.github.lucasbrovetto.financialintegration.acquirer.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum AuthorizationDecision {

    APPROVED("1", "Approve the sale", AuthorizationResponseCode.APPROVED),
    DO_NOT_HONOR("2", "Reject the sale without a specific reason", AuthorizationResponseCode.DO_NOT_HONOR),
    INSUFFICIENT_FUNDS("3", "Reject the sale because of insufficient funds", AuthorizationResponseCode.INSUFFICIENT_FUNDS),
    SYSTEM_MALFUNCTION("4", "Return an acquirer system error", AuthorizationResponseCode.SYSTEM_MALFUNCTION),
    TIMEOUT("5", "Do not answer and let the client timeout", null);

    private final String option;
    private final String description;
    private final String responseCode;

    public boolean sendsResponse() {
        return responseCode != null;
    }

    public static Optional<AuthorizationDecision> fromOption(String option) {
        return Arrays.stream(values())
                .filter(decision -> decision.getOption().equals(option))
                .findFirst();
    }
}
