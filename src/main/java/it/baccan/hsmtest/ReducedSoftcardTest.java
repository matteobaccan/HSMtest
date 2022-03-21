package it.baccan.hsmtest;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

import com.ncipher.provider.km.nCipherKM;
import it.baccan.hsmtest.softcardCreator.SoftcardCreator;

/**
 * This is a reduced test to exemplify a problem experienced using the library.
 *
 */
public class ReducedSoftcardTest {
    public void reducedTest() throws Exception {
        System.setProperty("NFJAVA_DEBUG", Integer.toString(8 + 4));
        System.setProperty("NFJAVA_DEBUGFILE", "ncipherreduced.log");

        KeyStore keyStore;
        String providerName = "nCipherKM";
        Provider securityProvider = Security.getProvider(providerName);
        Security.addProvider(new nCipherKM());
        Security.getProvider(providerName);

        // this works as expected
        String softcard1 = SoftcardCreator.create("C630-03E0-D947", "softcard1", "abc");
        System.setProperty("protect", "softcard:" + softcard1);
        keyStore = KeyStore.getInstance("ncipher.sworld", providerName);
        char[] pass1 = {'a', 'b', 'c'};
        String keystore1 = "keystore1.jks";
        keyStore.load(null, pass1);
        keyStore.store(new FileOutputStream(keystore1), pass1);

        // this raises Exception
        String softcard2 = SoftcardCreator.create("C630-03E0-D947", "softcard2", "cba");
        System.setProperty("protect", "softcard:" + softcard2);
        keyStore = KeyStore.getInstance("ncipher.sworld", providerName);
        char[] pass2 = {'c', 'b', 'a'};
        String keystore2 = "keystore2.jks";
        keyStore.load(null, pass2);
        keyStore.store(new FileOutputStream(keystore2), pass2);
    }

    public static void main(String[] args) {
        ReducedSoftcardTest rst = new ReducedSoftcardTest();
        try {
            rst.reducedTest();
            System.out.println("Success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}