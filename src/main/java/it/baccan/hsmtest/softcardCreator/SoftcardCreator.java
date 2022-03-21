package it.baccan.hsmtest.softcardCreator;

import com.ncipher.km.nfkm.CreateSoftCard;
import com.ncipher.km.nfkm.Module;
import com.ncipher.km.nfkm.SecurityWorld;
import com.ncipher.km.nfkm.SoftCard;
import com.ncipher.nfast.NFException;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * SoftcardCreator class.
 * </p>
 *
 * classe che crea una softcard
 *
 * @author Michele Costabile (m.costabile@rdigitale.eu)
 * @version $Id: $Id
 */
public class SoftcardCreator {

    private static final Logger log = LoggerFactory.getLogger(SoftcardCreator.class);

    private SoftcardCreator() {
    }

    /**
     * <p>
     * create.
     * </p>
     *
     * @param electronicSerialNumber a {@link java.lang.String} object.
     * @param softcardName a {@link java.lang.String} object.
     * @param pin a {@link java.lang.String} object.
     * @throws com.ncipher.nfast.NFException if any.
     */
    public static String create(String electronicSerialNumber, String softcardName, String pin) throws NFException {

        WorldCallbacks wcb = new WorldCallbacks(pin.toCharArray());
        try (SecurityWorld world = new SecurityWorld(null, wcb, null, true)) {
            Module mod = null;

            mod = findModule(world, electronicSerialNumber, mod);
            if (mod == null) {
                log.error("No module found for electronicSerialNumber: {}", electronicSerialNumber);
                return null;
            }

            SoftCardCallBack scb = new SoftCardCallBack(pin.toCharArray());

            try ( CreateSoftCard sc = new CreateSoftCard(scb, mod, false, softcardName)) {
                sc.go();
            }
            return findSoftCard(world, softcardName);
        }
    }

    /**
     * find a softcard with a given name. The most recent is chosen.
     *
     * @param electronicSerialNumber a {@link java.lang.String} object.
     * @param softcardName a {@link java.lang.String} object.
     * @return the softcard with the given name, or nu;ll
     * @throws com.ncipher.nfast.NFException if any.
     */
    public static String find(String electronicSerialNumber, String softcardName) throws NFException {
        String sc;

        try (SecurityWorld world = new SecurityWorld(null, null, null, true)) {
            Module mod = null;

            mod = findModule(world, electronicSerialNumber, mod);
            if (mod == null) {
                log.error("No module found for electronicSerialNumber: {}", electronicSerialNumber);
                return null;
            }

            sc = findSoftCard(world, softcardName);

            if (sc == null) {
                log.error("No Softcard found with name: {}", softcardName);
            }
        }
        return sc;
    }

    private static String findSoftCard(final SecurityWorld world, String softcardName) throws NFException {
        String hexID = null;
        long lowestTimediff = 0;
        Date today = Calendar.getInstance().getTime();
        for (SoftCard sx : world.getSoftCards()) {
            if (sx.getName().equals(softcardName)) {
                // the name is right
                long diffInMillies = Math.abs(sx.getCreationDate().getTime() - today.getTime());
                String ID = String.format("%040x", new java.math.BigInteger(1, sx.getID().value));
                if (lowestTimediff == 0 || diffInMillies < lowestTimediff) {
                    // the date is newer
                    lowestTimediff = diffInMillies;
                    hexID = ID;
                    log.debug("softcard considered, ID: {}, {}, made {} milliseconds ago",
                            sx.getName(), hexID, diffInMillies);
                } else {
                    // TODO: considerar a possibile sx.erase();
                    log.debug("softcard {}, ID: {}, made {} milliseconds ago, discarded",
                            sx.getName(), ID, diffInMillies);
                }
            }
        }
        return hexID;
    }

    private static Module findModule(final SecurityWorld world, String electronicSerialNumber, Module mod) throws NFException {
        /*
        * Find module with a given ESN
         */
        final Module[] modules = world.getModules();
        for (Module m : modules) {
            if (m.getESN().equals(electronicSerialNumber)) {
                mod = m;
                break;
            }
        }
        return mod;
    }
}
