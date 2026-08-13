package io.github.lucasbrovetto.financialintegration.acquirer.adapter.tcp;

import io.github.lucasbrovetto.financialintegration.acquirer.domain.AuthorizationDecision;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcquirerTcpCommunicationTest {

    private static final int CONCURRENT_CLIENTS = 50;

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void receivesRealTcpRequestAndReturnsSelectedResponse() throws Exception {
        ISOPackager packager = loadPackager();
        AuthorizationRequestListener listener =
                new AuthorizationRequestListener(request -> AuthorizationDecision.APPROVED);

        try (VirtualThreadAcquirerServer server = new VirtualThreadAcquirerServer(0, packager, listener)) {
            Thread serverThread = Thread.ofVirtual()
                    .name("acquirer-test-acceptor")
                    .start(() -> start(server));
            ASCIIChannel client = new ASCIIChannel("127.0.0.1", server.getPort(), packager);
            client.setLengthDigits(4);
            client.connect();
            client.setTimeout(2_000);
            try {
                client.send(authorizationRequest(packager));

                ISOMsg response = client.receive();

                assertEquals("0210", response.getMTI());
                assertEquals("00", response.getString(39));
                assertEquals("123456", response.getString(11));
                assertEquals("626216123456", response.getString(37));
            } finally {
                client.disconnect();
            }
            server.close();
            serverThread.join(1_000);
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void handlesManyConcurrentConnectionsWithVirtualThreads() throws Exception {
        ISOPackager packager = loadPackager();
        AuthorizationRequestListener listener =
                new AuthorizationRequestListener(request -> AuthorizationDecision.APPROVED);

        try (VirtualThreadAcquirerServer server = new VirtualThreadAcquirerServer(0, packager, listener);
             var clients = Executors.newVirtualThreadPerTaskExecutor()) {
            Thread serverThread = Thread.ofVirtual()
                    .name("acquirer-concurrent-test-acceptor")
                    .start(() -> start(server));

            List<Future<ISOMsg>> responses = IntStream.range(0, CONCURRENT_CLIENTS)
                    .mapToObj(sequence -> clients.submit(() -> exchange(server.getPort(), packager, sequence)))
                    .toList();

            for (Future<ISOMsg> response : responses) {
                ISOMsg message = response.get();
                assertEquals("0210", message.getMTI());
                assertEquals("00", message.getString(39));
            }

            server.close();
            serverThread.join(1_000);
        }
    }

    private ISOMsg exchange(int port, ISOPackager packager, int sequence) throws Exception {
        ASCIIChannel client = new ASCIIChannel("127.0.0.1", port, packager);
        client.setLengthDigits(4);
        client.connect();
        client.setTimeout(2_000);
        try {
            client.send(authorizationRequest(packager, sequence));
            return client.receive();
        } finally {
            client.disconnect();
        }
    }

    private void start(VirtualThreadAcquirerServer server) {
        try {
            server.start();
        } catch (java.io.IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private ISOMsg authorizationRequest(ISOPackager packager) throws Exception {
        return authorizationRequest(packager, 123_456);
    }

    private ISOMsg authorizationRequest(ISOPackager packager, int sequence) throws Exception {
        ISOMsg request = new ISOMsg();
        request.setPackager(packager);
        request.setMTI("0200");
        request.set(3, "000000");
        request.set(4, "000000010000");
        request.set(7, "0804163000");
        String stan = "%06d".formatted(sequence);
        request.set(11, stan);
        request.set(37, "626216" + stan);
        request.set(41, "TERM0001");
        request.set(49, "858");
        return request;
    }

    private ISOPackager loadPackager() throws Exception {
        InputStream configuration = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("cfg/packager/acquirer.xml");
        return new GenericPackager(configuration);
    }
}
