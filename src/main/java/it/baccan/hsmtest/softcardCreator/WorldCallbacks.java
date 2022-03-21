/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.baccan.hsmtest.softcardCreator;

import com.ncipher.km.nfkm.CmdCallBack;
import com.ncipher.km.nfkm.Slot;

/**
 * una classe che riceve chiamate dall'interfaccia Thales verso HSM
 *
 * @author gabri
 */
class WorldCallbacks implements CmdCallBack {

    private final char[] pin;

    /**
     * <p>
     * Constructor for WorldCallbacks.</p>
     *
     * @param pin an array of {@link char} objects.
     */
    public WorldCallbacks(char[] pin) {
        this.pin = pin;
    }

    /** {@inheritDoc} */
    @Override
    public boolean errorCallBack(String string, String string1, String string2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /** {@inheritDoc} */
    @Override
    public Slot reqCardCallBack(String string, String string1, int i, Slot slot) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /** {@inheritDoc} */
    @Override
    public String reqPPCallBack(String action) {
        return new String(pin);
    }

}
