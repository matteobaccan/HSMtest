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
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Matteo Baccan
 */
@Slf4j
public class HSMfeature {

    public static void main(String[] args) {
        log.info("Start test");

        try {
            SecurityWorld securityWorld = new SecurityWorld(new ConsoleCallBack());

            Key[] keys = securityWorld.listKeys("simple");
            log.info("Keys:" + keys.length);
            Arrays.asList(keys).forEach(key -> log.info(key.toString()));

            Module[] modules = securityWorld.getModules();
            log.info("Modules:" + modules.length);
            Arrays.asList(modules).forEach(module -> log.info(module.toString()));
            if (securityWorld.isRecoveryEnabled()) {
                log.info("Security world has key recovery enabled.");
            }

        } catch (NFException nFException) {
            log.info(nFException.getMessage());
        }

        log.info("End test");
    }

}
