/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.baccan.hsmtest;

import com.ncipher.km.nfkm.CardSet;
import com.ncipher.km.nfkm.ConsoleCallBack;
import com.ncipher.km.nfkm.Key;
import com.ncipher.km.nfkm.KeyGenCallBack;
import com.ncipher.km.nfkm.KeyGenerator;
import com.ncipher.km.nfkm.Module;
import com.ncipher.km.nfkm.SecurityWorld;
import com.ncipher.km.nfkm.Slot;
import com.ncipher.km.nfkm.SoftCard;
import com.ncipher.nfast.NFException;
import com.ncipher.nfast.connect.ClientException;
import com.ncipher.nfast.connect.CommandTooBig;
import com.ncipher.nfast.connect.ConnectionClosed;
import com.ncipher.nfast.connect.NFConnection;
import com.ncipher.nfast.connect.StatusNotOK;
import com.ncipher.nfast.marshall.M_Act_Details_OpPermissions;
import com.ncipher.nfast.marshall.M_ByteBlock;
import com.ncipher.nfast.marshall.M_Cmd;
import com.ncipher.nfast.marshall.M_Cmd_Args_GenerateKeyPair;
import com.ncipher.nfast.marshall.M_Cmd_Args_GetApplianceTime;
import com.ncipher.nfast.marshall.M_Cmd_Args_GetApplianceVersion;
import com.ncipher.nfast.marshall.M_Cmd_Args_NVMemList;
import com.ncipher.nfast.marshall.M_Cmd_Args_NoOp;
import com.ncipher.nfast.marshall.M_Command;
import com.ncipher.nfast.marshall.M_Reply;
import com.ncipher.nfast.marshall.M_Status;
import com.ncipher.nfast.marshall.M_Cmd_Args_Encrypt;
import com.ncipher.nfast.marshall.M_Cmd_Reply_Encrypt;
import com.ncipher.nfast.marshall.M_Mech;
import com.ncipher.nfast.marshall.M_Mech_Cipher_Generic128;
import com.ncipher.nfast.marshall.M_PlainText;
import com.ncipher.nfast.marshall.M_PlainTextType;
import com.ncipher.nfast.marshall.M_PlainTextType_Data_Bytes;
import com.ncipher.nfast.marshall.Marshallable;
import com.ncipher.nfast.marshall.PrintoutContext;
import com.ncipher.nfast.connect.utils.EasyConnection;
import com.ncipher.nfast.marshall.M_ChannelMode;
import com.ncipher.nfast.marshall.M_CipherText;
import com.ncipher.nfast.marshall.M_Cmd_Args_Decrypt;
import com.ncipher.nfast.marshall.M_Cmd_Reply_Decrypt;
import com.ncipher.nfast.marshall.M_IV;
import com.ncipher.nfast.marshall.M_KeyGenParams;
import com.ncipher.nfast.marshall.M_KeyType;
import com.ncipher.nfast.marshall.M_KeyType_GenParams_Random;
import com.ncipher.nfast.marshall.MarshallTypeError;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Matteo Baccan
 */
@Slf4j
public class HSMfeature {

    public static void main(String[] args) {
        HSMfeature hSMfeature = new HSMfeature();
        hSMfeature.run();
    }

    public void run() {
        log.info("Start test");

        try {
            // Connesione a security World
            SecurityWorld sw = new SecurityWorld(new ConsoleCallBack());

            // Elenco chiavi
            Key[] keys = sw.listKeys("simple");
            log.info("Keys:" + keys.length);
            Arrays.asList(keys).forEach(key -> log.info(key.toString()));

            // Elenco moduli
            Module[] modules = sw.getModules();
            log.info("Modules:" + modules.length);
            Arrays.asList(modules).forEach(module -> {
                log.info(module.toString());
                Slot[] slots = module.getSlots();
                log.info(" Slots:" + slots.length);
                Arrays.asList(slots).forEach(slot -> log.info(" {}", mapParameter(slot.getData())));
            });

            // Elenco SoftCard
            SoftCard[] softCards = sw.getSoftCards();
            log.info("SoftCards:" + softCards.length);
            Arrays.asList(softCards).forEach(softCard -> log.info(softCard.toString()));

            // Elenco CardSet
            CardSet[] cardSets = sw.getCardSets(null);
            log.info("CardSets:" + cardSets.length);
            Arrays.asList(cardSets).forEach(cardSet -> log.info(cardSet.toString()));

            if (sw.isRecoveryEnabled()) {
                log.info("Security world has key recovery enabled.");
            }

            // Connessione diretta per comandi
            NFConnection conn = new NFConnection(NFConnection.flags_Privileged);

            // NoOp
            M_Cmd_Args_NoOp commandArgsNoOp = new M_Cmd_Args_NoOp(modules[0].getID());
            sendCommand("NoOp", conn, new M_Command(M_Cmd.NoOp, 0, commandArgsNoOp));

            // MemList
            M_Cmd_Args_NVMemList commandArgsNVMemList = new M_Cmd_Args_NVMemList(modules[0].getID(), 0);
            sendCommand("NVMMemList", conn, new M_Command(M_Cmd.NVMemList, 0, commandArgsNVMemList));

            // ListRemoteSlot
            M_Cmd_Args_GetApplianceTime commandArgsGetApplianceTime = new M_Cmd_Args_GetApplianceTime(modules[0].getID(), 0);
            sendCommand("GetApplianceTime", conn, new M_Command(M_Cmd.GetApplianceTime, 0, commandArgsGetApplianceTime));

            // Version HSM
            M_Cmd_Args_GetApplianceVersion commandArgsGetApplianceVersion = new M_Cmd_Args_GetApplianceVersion(modules[0].getID());
            sendCommand("GetApplianceVersion", conn, new M_Command(M_Cmd.GetApplianceVersion, 0, commandArgsGetApplianceVersion));

            // Genera una chiave RSA
            KeyGenerator kg = sw.getKeyGenerator();
            Module module = sw.getModule(1);
            Key k = kg.generateRSA(2048, "rsakey11", "simple", "rsakey", null, module, null, true);
            log.debug("RSA public key : [{}]", mapParameter(k.exportPublic()));

            // Genera una chiave DES
            Key kDES = kg.generateDES("deskey11", module, null);
            log.debug("DES Key : [{}]", mapParameter(kDES.getData()));

            // Genera una chiave per l'encryption
            M_KeyGenParams params = new M_KeyGenParams(M_KeyType.Rijndael, new M_KeyType_GenParams_Random(32));
            Key rijndael = kg.generateKey(params, "keyname", "appname", "ident", null, module, null, null);
            log.debug("Rijndael Key : [{}]", mapParameter(rijndael.getData()));

            // Encrypt del dato
            byte[] crypt = null;
            try {
                EasyConnection easyConnection = new EasyConnection(conn);
                crypt = easyConnection.SymmetricCrypt(M_ChannelMode.Encrypt,
                        rijndael.getKeyID(module),
                        M_Mech.RijndaelmECBpPKCS5,
                        "ciao".getBytes(),
                        null,
                        null,
                        true,
                        false);
            } catch (NFException nFException) {
                log.info("NFException", nFException);
            }
            // Encrypt del dato
            byte[] aescrypt = aesEncrypt(conn, rijndael, "QuestoStreamDeveEssereCifrato".getBytes());
            log.debug("Crypt[{}]", aescrypt);

            byte[] aesdecrypt = aesDecrypt(conn, k, crypt);
            log.debug("Derypt[{}]", aesdecrypt);

        } catch (NFException nFException) {
            log.info("NFException", nFException);
        }

        log.info("End test");
    }

    private M_Reply sendCommand(final String command, final NFConnection conn, final M_Command mCommand) throws NFException {
        log.info("Command [{}] ------------------------------------", command);
        log.info("Command args [{}]", mapParameter(mCommand.args));
        log.info("Command certs [{}]", mCommand.certs);
        log.info("Command cmd [{}]", mCommand.cmd);
        log.info("Command extractstate [{}]", mCommand.extractstate);
        log.info("Command flags [{}]", mCommand.flags);
        log.info("Command services [{}]", mCommand.services);
        log.info("Command state [{}]", mCommand.state);
        log.info("Command status [{}]", mCommand.status);
        log.info("Command tag [{}]", mapParameter(mCommand.tag));
        M_Reply reply = conn.transact(mCommand);
        if (reply.status == M_Status.OK) {
            log.info("Reply [{}]", mapParameter(reply.reply));
        } else {
            log.error("Reply error [{}]", mapParameter(reply));
        }
        return reply;
    }

    private String mapParameter(final Marshallable marshallable) {
        return Optional.ofNullable(marshallable).map(mapper -> {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);
            PrintoutContext printoutContext = new PrintoutContext(printWriter);
            mapper.printout(printoutContext);
            printWriter.flush();
            return byteArrayOutputStream.toString();
        }).orElse("").trim();
    }

    /*
    private M_KeyID loadKey(Key key, Module module, Slot slot) throws NFException {
        M_KeyID keyID;
        // Load key
        if (key.isCardSetProtected()) {
            CardSet cs = key.getCardSet();
            cs.load(slot, new ConsoleCallBack());
            keyID = key.load(cs, module);
        } else if (key.isSoftCardProtected()) {
            SoftCard sc = key.getSoftCard();
            sc.load(module, new ConsoleCallBack());
            keyID = key.load(sc, module);
        } else {
            keyID = key.load(module);
        }
        return keyID;
    }
     */
    class ChangeACLCallback extends KeyGenCallBack {

        @Override
        public M_Command modifyGenerateKeyCmd(M_Command cmd) {
            // Add encrypt permission
            ((M_Act_Details_OpPermissions) ((M_Cmd_Args_GenerateKeyPair) cmd.args).aclpriv.groups[0].actions[0].details).perms |= M_Act_Details_OpPermissions.perms_Encrypt;
            return cmd;
        }
    }

    public byte[] aesEncrypt(final NFConnection conn, Key key, byte[] plain) throws NFException {
        M_Command cmd = new M_Command();
        cmd.cmd = M_Cmd.Encrypt;
        M_Cmd_Args_Encrypt args = new M_Cmd_Args_Encrypt();
        args.key = key.mergeKeyIDs();
        args.mech = M_Mech.RijndaelmECBpPKCS5;
        args.plain = new M_PlainText();
        args.plain.type = M_PlainTextType.Bytes;
        args.plain.data = new M_PlainTextType_Data_Bytes();
        ((M_PlainTextType_Data_Bytes) args.plain.data).data = new M_ByteBlock(plain);
        cmd.args = args;
        // Transact the command
        M_Reply rep = conn.transact(cmd);
        if (rep.status != M_Status.OK) {
            log.error("Error: [{}]", mapParameter(rep));
            throw new NFException("Command failed");
        }
        return ((M_Mech_Cipher_Generic128) ((M_Cmd_Reply_Encrypt) rep.reply).cipher.data).cipher.value;
    }

    public byte[] aesDecrypt(final NFConnection conn, Key key, byte[] ciphertext) throws NFException {
        M_Command cmd = new M_Command();
        cmd.cmd = M_Cmd.Decrypt;
        M_Cmd_Args_Decrypt args = new M_Cmd_Args_Decrypt();
        args.key = key.mergeKeyIDs();
        args.mech = M_Mech.RijndaelmECBpPKCS5;
        args.reply_type = M_PlainTextType.Bytes;
        args.cipher = new M_CipherText();
        args.cipher.mech = M_Mech.RijndaelmECBpPKCS5;
        args.cipher.data = new M_Mech_Cipher_Generic128();
        ((M_Mech_Cipher_Generic128) args.cipher.data).cipher = new M_ByteBlock(ciphertext);
        cmd.args = args;
        // Transact the command
        M_Reply rep = conn.transact(cmd);
        if (rep.status != M_Status.OK) {
            log.error("Error: [{}]", mapParameter(rep));
            throw new NFException("Command failed.\n");
        }
        return ((M_PlainTextType_Data_Bytes) ((M_Cmd_Reply_Decrypt) rep.reply).plain.data).data.value;
    }

}
