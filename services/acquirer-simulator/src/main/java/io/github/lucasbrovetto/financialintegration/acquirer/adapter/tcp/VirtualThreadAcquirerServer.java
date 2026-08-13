package io.github.lucasbrovetto.financialintegration.acquirer.adapter.tcp;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.channel.ASCIIChannel;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public final class VirtualThreadAcquirerServer implements AutoCloseable {

    private static final int LENGTH_HEADER_DIGITS = 4;

    private final ISOPackager packager;
    private final ISORequestListener requestListener;
    private final ServerSocket serverSocket;
    private final ExecutorService connections = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicBoolean running = new AtomicBoolean();

    @Getter
    private final int port;

    public VirtualThreadAcquirerServer(
            int port,
            ISOPackager packager,
            ISORequestListener requestListener) throws IOException {

        this.packager = packager;
        this.requestListener = requestListener;
        this.serverSocket = new ServerSocket(port);
        this.port = serverSocket.getLocalPort();
    }

    public void start() throws IOException {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Acquirer server is already running");
        }

        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                log.debug("Accepted acquirer connection from {}", socket.getRemoteSocketAddress());
                connections.submit(() -> handleConnection(socket));
            } catch (SocketException exception) {
                if (running.get()) {
                    throw exception;
                }
            }
        }
    }

    private void handleConnection(Socket socket) {
        ConnectedAsciiChannel channel = new ConnectedAsciiChannel(packager);
        try {
            channel.attach(socket);
            while (running.get() && channel.isConnected()) {
                ISOMsg request = channel.receive();
                requestListener.process(channel, request);
            }
        } catch (EOFException | SocketException exception) {
            log.debug("Acquirer client disconnected: {}", socket.getRemoteSocketAddress());
        } catch (IOException | ISOException exception) {
            log.error("ISO 8583 connection failed for {}", socket.getRemoteSocketAddress(), exception);
        } finally {
            disconnect(channel);
        }
    }

    private void disconnect(ASCIIChannel channel) {
        if (!channel.isConnected()) {
            return;
        }
        try {
            channel.disconnect();
        } catch (IOException exception) {
            log.warn("Could not close acquirer connection", exception);
        }
    }

    @Override
    public void close() {
        running.set(false);
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException exception) {
            log.warn("Could not close acquirer server", exception);
        } finally {
            connections.shutdownNow();
            log.info("Acquirer simulator stopped");
        }
    }

    private static final class ConnectedAsciiChannel extends ASCIIChannel {

        ConnectedAsciiChannel(ISOPackager packager) {
            setPackager(packager);
            setLengthDigits(LENGTH_HEADER_DIGITS);
        }

        void attach(Socket socket) throws IOException {
            connect(socket);
        }
    }
}
