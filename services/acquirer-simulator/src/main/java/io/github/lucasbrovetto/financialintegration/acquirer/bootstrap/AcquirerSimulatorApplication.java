package io.github.lucasbrovetto.financialintegration.acquirer.bootstrap;

import io.github.lucasbrovetto.financialintegration.acquirer.adapter.tcp.AuthorizationRequestListener;
import io.github.lucasbrovetto.financialintegration.acquirer.adapter.tcp.VirtualThreadAcquirerServer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;

import java.io.InputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AcquirerSimulatorApplication {

    private static final int DEFAULT_PORT = 9000;
    private static final String PACKAGER_RESOURCE = "cfg/packager/acquirer.xml";

    public static void main(String[] args) throws Exception {
        int port = resolvePort();
        ISOPackager packager = loadPackager();
        try (VirtualThreadAcquirerServer server = new VirtualThreadAcquirerServer(
                port,
                packager,
                new AuthorizationRequestListener())) {
            Runtime.getRuntime().addShutdownHook(new Thread(server::close, "acquirer-shutdown"));

            log.info("Acquirer simulator started on TCP port {}", server.getPort());
            log.info("Each client connection is handled by a Java virtual thread");
            log.info("Waiting for ISO 8583 authorization requests");

            server.start();
        }
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

    private static int resolvePort() {
        String configuredPort = System.getenv("ACQUIRER_PORT");
        if (configuredPort == null || configuredPort.isBlank()) {
            return DEFAULT_PORT;
        }

        try {
            int port = Integer.parseInt(configuredPort);
            if (port < 1 || port > 65_535) {
                throw new IllegalArgumentException("ACQUIRER_PORT must be between 1 and 65535");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("ACQUIRER_PORT must be a valid TCP port", exception);
        }
    }
}
