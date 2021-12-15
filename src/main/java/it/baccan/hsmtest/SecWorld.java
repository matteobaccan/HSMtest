/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.baccan.hsmtest;

import com.ncipher.km.nfkm.ConsoleCallBack;
import com.ncipher.km.nfkm.Key;
import com.ncipher.km.nfkm.Module;
import com.ncipher.km.nfkm.SecurityWorld;
import com.ncipher.nfast.NFException;

/**
 *
 * @author Administrator
 */
public class SecWorld {

    public static void main(String[] args) {
        try {
            // ConsoleCallBack is perfect for command line applications
            SecurityWorld sw = new SecurityWorld(new ConsoleCallBack());
            // Pass null to list all keys
            Key[] keys = sw.listKeys("simple");
            System.out.println("There are " + keys.length + " 'simple' keys.");
            for (int i = 0; i < keys.length; i++) {
                System.out.format("chiave %d, nome %s\n", i, keys[i].getName());
            }
            Module[] modules = sw.getModules();
            System.out.println("There are " + modules.length + " HSMs available.");
            for (int i = 0; i < modules.length; i++) {
                System.out.format("chiave %d, ESN %s\n", i, modules[i].getESN());
            }
            if (sw.isRecoveryEnabled()) {
                System.out.println("Security world has key recovery enabled.");
            }
        } catch (NFException e) {
            // An error occurred in the NFKM libraries
            // (note: this try-catch omitted in later examples)
            e.printStackTrace();
        }
    }
}
