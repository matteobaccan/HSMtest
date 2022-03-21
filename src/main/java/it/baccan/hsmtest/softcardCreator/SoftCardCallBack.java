/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.baccan.hsmtest.softcardCreator;

import com.ncipher.km.nfkm.DefaultCallBack;

/**
 * @author gabri
 */
class SoftCardCallBack extends DefaultCallBack {

    private char[] pin;

    /**
     * <p>
     * Constructor for SoftCardCallBack.
     * </p>
     *
     * @param pin an array of {@link char} objects.
     */
    public SoftCardCallBack(char[] pin) {
        this.pin = pin;
    }

    /** {@inheritDoc} */
    @Override
    public String reqPPCallBack(String action) {
        return new String(pin);
    }
}
