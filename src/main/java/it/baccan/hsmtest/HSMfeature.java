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
import com.ncipher.nfast.connect.NFConnection;
import com.ncipher.nfast.marshall.M_Cmd;
import com.ncipher.nfast.marshall.M_Cmd_Args_NVMemList;
import com.ncipher.nfast.marshall.M_Cmd_Args_NoOp;
import com.ncipher.nfast.marshall.M_Command;
import com.ncipher.nfast.marshall.M_Reply;
import com.ncipher.nfast.marshall.M_Status;
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
            // Connesione a security World
            SecurityWorld securityWorld = new SecurityWorld(new ConsoleCallBack());

            // Elenco chiavi
            Key[] keys = securityWorld.listKeys("simple");
            log.info("Keys:" + keys.length);
            Arrays.asList(keys).forEach(key -> log.info(key.toString()));

            // Elenco moduli
            Module[] modules = securityWorld.getModules();
            log.info("Modules:" + modules.length);
            Arrays.asList(modules).forEach(module -> log.info(module.toString()));
            if (securityWorld.isRecoveryEnabled()) {
                log.info("Security world has key recovery enabled.");
            }

            // Connessione diretta per comandi
            NFConnection nFConnection = new NFConnection();

            // NoOp
            M_Cmd_Args_NoOp commandArgsNoOp = new M_Cmd_Args_NoOp(modules[0].getID());
            M_Command mCommandNoOp = new M_Command(M_Cmd.NoOp, 0, commandArgsNoOp);
            M_Reply mReplyNoOp = sendCommand(nFConnection, mCommandNoOp);
            log.info("NoOp [{}]", mReplyNoOp);

            // MeMList
            M_Cmd_Args_NVMemList commandArgs = new M_Cmd_Args_NVMemList(modules[0].getID(), 0);
            M_Command mCommand = new M_Command(M_Cmd.NVMemList, 0, commandArgs);
            M_Reply mReply = sendCommand(nFConnection, mCommand);
            log.info("NVMMemList [{}]", mReply);
        } catch (NFException nFException) {
            log.info("NFException", nFException);
        }

        log.info("End test");
    }

    private static M_Reply sendCommand(final NFConnection nFConnection, final M_Command mCommand) throws NFException {
        log.info("Command [{}]", mCommand.toString());
        M_Reply reply = nFConnection.transact(mCommand);
        if (reply.status == M_Status.OK) {
            log.info("Reply [{}]", reply.reply);
        } else {
            throw new NFException(M_Status.toString(reply.status));
        }
        return reply;
    }

}
