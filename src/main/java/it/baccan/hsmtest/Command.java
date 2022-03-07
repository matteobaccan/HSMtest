/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.baccan.hsmtest;

import com.ncipher.km.nfkm.ConsoleCallBack;
import com.ncipher.km.nfkm.SecurityWorld;
import com.ncipher.nfast.NFException;
import com.ncipher.nfast.connect.NFConnection;
import com.ncipher.nfast.marshall.M_Cmd;
import com.ncipher.nfast.marshall.M_Cmd_Args_CheckWorld;
import com.ncipher.nfast.marshall.M_Cmd_Args_NoOp;
import com.ncipher.nfast.marshall.M_Cmd_Reply_CheckWorld;
import com.ncipher.nfast.marshall.M_Command;
import com.ncipher.nfast.marshall.M_Reply;
import com.ncipher.nfast.marshall.M_Status;

/**
 * Questa classe mostra il minimo necessario per lanciare un comando su un HSM e
 * ricevere una risposta. In questo caso, usiamo il comando M_Cmd.NoOp, che
 * riceve unicamente una conferma di esecuzione e il comando M_Cmd.CheckWorld,
 * che riceve una risposta di tipo M_Cmd_Reply_CheckWorld da cui possiamo
 * estrarre informazioni.
 *
 * @author Michele Costabile
 */
public class Command {

    public static void main(String[] args) {
        try {
            int modNumber = 2;
            // ConsoleCallBack is perfect for command line applications
            SecurityWorld sw = new SecurityWorld(new ConsoleCallBack());
            NFConnection conn = sw.getConnection();

            M_Command cmd = new M_Command();
            cmd.cmd = M_Cmd.NoOp;
            cmd.args = new M_Cmd_Args_NoOp(sw.getModule(modNumber).getID());

            conn.submit(cmd);
            M_Reply reply = conn.transact(cmd);
            if (reply.status == M_Status.OK) {
                System.out.println("success!!");
            } else {
                throw new NFException(M_Status.toString(reply.status));
            }

            cmd.cmd = M_Cmd.CheckWorld;
            cmd.args = new M_Cmd_Args_CheckWorld(sw.getModule(modNumber).getID());

            conn.submit(cmd);
            reply = conn.transact(cmd);
            if (reply.status == M_Status.OK) {
                M_Cmd_Reply_CheckWorld rep = (M_Cmd_Reply_CheckWorld) reply.reply;
                System.out.format("success!! %d seconds", rep.age.value);
            } else {
                throw new NFException(M_Status.toString(reply.status));
            }

        } catch (NFException e) {
            // An error occurred in the NFKM libraries
            // (note: this try-catch omitted in later examples)
            e.printStackTrace();
        }
    }
}
