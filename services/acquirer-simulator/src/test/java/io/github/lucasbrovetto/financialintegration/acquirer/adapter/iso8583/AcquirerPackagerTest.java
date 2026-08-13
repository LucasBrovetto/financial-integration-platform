package io.github.lucasbrovetto.financialintegration.acquirer.adapter.iso8583;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcquirerPackagerTest {

    @Test
    void packsAndUnpacksAuthorizationRequest() throws Exception {
        InputStream packagerConfiguration = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("cfg/packager/acquirer.xml");
        GenericPackager packager = new GenericPackager(packagerConfiguration);
        ISOMsg request = new ISOMsg();
        request.setPackager(packager);
        request.setMTI("0200");
        request.set(3, "000000");
        request.set(4, "000000015050");
        request.set(7, "0804134500");
        request.set(11, "000001");
        request.set(37, "626216000001");
        request.set(41, "TERM0001");
        request.set(49, "858");

        byte[] packed = request.pack();
        ISOMsg unpacked = new ISOMsg();
        unpacked.setPackager(packager);
        unpacked.unpack(packed);

        assertEquals("0200", unpacked.getMTI());
        assertEquals("000000015050", unpacked.getString(4));
        assertEquals("000001", unpacked.getString(11));
        assertEquals("626216000001", unpacked.getString(37));
        assertEquals("TERM0001", unpacked.getString(41));
        assertEquals("858", unpacked.getString(49));
    }
}
