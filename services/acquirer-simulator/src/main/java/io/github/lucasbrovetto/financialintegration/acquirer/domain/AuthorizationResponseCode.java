package io.github.lucasbrovetto.financialintegration.acquirer.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthorizationResponseCode {

    public static final String APPROVED = "00";
    public static final String DO_NOT_HONOR = "05";
    public static final String INSUFFICIENT_FUNDS = "51";
    public static final String SYSTEM_MALFUNCTION = "96";

}
