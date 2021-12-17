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
import com.ncipher.nfast.connect.NFConnection;
import com.ncipher.nfast.marshall.M_Act_Details_OpPermissions;
import com.ncipher.nfast.marshall.M_ByteBlock;
import com.ncipher.nfast.marshall.M_CipherText;
import com.ncipher.nfast.marshall.M_Cmd;
import com.ncipher.nfast.marshall.M_Cmd_Args_Decrypt;
import com.ncipher.nfast.marshall.M_Cmd_Args_Encrypt;
import com.ncipher.nfast.marshall.M_Cmd_Args_GenerateKeyPair;
import com.ncipher.nfast.marshall.M_Cmd_Args_GetApplianceTime;
import com.ncipher.nfast.marshall.M_Cmd_Args_GetApplianceVersion;
import com.ncipher.nfast.marshall.M_Cmd_Args_NVMemList;
import com.ncipher.nfast.marshall.M_Cmd_Args_NoOp;
import com.ncipher.nfast.marshall.M_Cmd_Reply_Decrypt;
import com.ncipher.nfast.marshall.M_Cmd_Reply_Encrypt;
import com.ncipher.nfast.marshall.M_Command;
import com.ncipher.nfast.marshall.M_KeyGenParams;
import com.ncipher.nfast.marshall.M_KeyID;
import com.ncipher.nfast.marshall.M_KeyType;
import com.ncipher.nfast.marshall.M_KeyType_GenParams_Random;
import com.ncipher.nfast.marshall.M_Mech;
import com.ncipher.nfast.marshall.M_Mech_Cipher_Generic128;
import com.ncipher.nfast.marshall.M_PlainText;
import com.ncipher.nfast.marshall.M_PlainTextType;
import com.ncipher.nfast.marshall.M_PlainTextType_Data_Bytes;
import com.ncipher.nfast.marshall.M_Reply;
import com.ncipher.nfast.marshall.M_Status;
import com.ncipher.nfast.marshall.Marshallable;
import com.ncipher.nfast.marshall.PrintoutContext;
import com.ncipher.provider.km.KMRSAPrivateKey;
import com.ncipher.provider.km.KMRSAPublicKey;
import com.ncipher.provider.km.nCipherKM;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Matteo Baccan
 */
@Slf4j
public class HSMfeature {

    int moduleNumber = 2;

    public static void main(String[] args) {
        HSMfeature hSMfeature = new HSMfeature();
        hSMfeature.run();
    }

    public void run() {
        log.info("Start test");

        try {
            // Connessione diretta per comandi
            NFConnection conn = nCipherKM.getConnection();

            // Connesione a security World
            SecurityWorld sw = new SecurityWorld(conn, new ConsoleCallBack());

            // Elenco moduli
            Module[] modules = sw.getModules();
            log.info("Modules:" + modules.length);
            Arrays.asList(modules).forEach(module -> {
                log.info(module.toString());
                // Elenco chiavi
                try {
                    printKeys(sw, module);
                } catch (NFException nFException) {
                    log.error("FException", nFException);
                }
                // Slot
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

            // Preparo il modulo 1
            Module module1 = sw.getModule(1);

            // NoOp
            M_Cmd_Args_NoOp commandArgsNoOp = new M_Cmd_Args_NoOp(module1.getID());
            sendCommand("NoOp", conn, new M_Command(M_Cmd.NoOp, 0, commandArgsNoOp));

            // MemList
            M_Cmd_Args_NVMemList commandArgsNVMemList = new M_Cmd_Args_NVMemList(module1.getID(), 0);
            sendCommand("NVMMemList", conn, new M_Command(M_Cmd.NVMemList, 0, commandArgsNVMemList));

            // ListRemoteSlot
            M_Cmd_Args_GetApplianceTime commandArgsGetApplianceTime = new M_Cmd_Args_GetApplianceTime(module1.getID(), 0);
            sendCommand("GetApplianceTime", conn, new M_Command(M_Cmd.GetApplianceTime, 0, commandArgsGetApplianceTime));

            // Version HSM
            M_Cmd_Args_GetApplianceVersion commandArgsGetApplianceVersion = new M_Cmd_Args_GetApplianceVersion(module1.getID());
            sendCommand("GetApplianceVersion", conn, new M_Command(M_Cmd.GetApplianceVersion, 0, commandArgsGetApplianceVersion));

            // Genera una chiave RSA
            KeyGenerator kg = sw.getKeyGenerator();
            Key k = kg.generateRSA(2048, "rsakey11", "simple", "rsakey", null, module1, null, true);
            log.debug("RSA public key : [{}]", mapParameter(k.exportPublic()));

            // Genera una chiave DES
            Key kDES = kg.generateDES("deskey11", module1, null);
            log.debug("DES Key : [{}]", mapParameter(kDES.getData()));

            // Genera una chiave per l'encryption
            M_KeyGenParams params = new M_KeyGenParams(M_KeyType.Rijndael, new M_KeyType_GenParams_Random(32));
            Key rijndael = kg.generateKey(params, "keyname", "simple", "ident", null, module1, null, null);
            log.debug("Rijndael Key : [{}]", mapParameter(rijndael.getData()));

            // Encrypt del dato
            final String datoDaCifrare = "QuestoStreamDeveEssereCifrato";
            byte[] aescrypt = aesEncrypt(conn, rijndael, datoDaCifrare.getBytes());
            log.debug("Crypt[{}]", aescrypt);

            byte[] aesdecrypt = aesDecrypt(conn, rijndael, rijndael.mergeKeyIDs(), aescrypt);
            log.debug("Decrypt[{}]", new String(aesdecrypt));
            log.debug("Verifica[{}]", new String(aesdecrypt).equals(datoDaCifrare));

            // Carico le chiavi nel secondo modulo
            Module module2 = sw.getModule(2);
            printKey(k, module1);
            printKey(k, module2);
            k.load(module2);
            printKey(k, module2);

            printKey(kDES, module1);
            printKey(kDES, module2);
            kDES.load(module2);
            printKey(kDES, module2);

            // Chiave solo sul primo HSM
            printKey(rijndael, module1);
            printKey(rijndael, module2);
            aesdecrypt = aesDecrypt(conn, rijndael, rijndael.getKeyID(module1), aescrypt);
            log.debug("Decrypt[{}]", new String(aesdecrypt));

            // Errore perchè la chiave non è caricata sul secondo modulo
            try {
                aesdecrypt = aesDecrypt(conn, rijndael, rijndael.getKeyID(module2), aescrypt);
                log.debug("Decrypt[{}]", new String(aesdecrypt));
            } catch (Throwable throwable) {
                log.debug("E' corretto ci sia un errore");
                log.info("Throwable", throwable);
            }
            // Descript con la mergedKeyIDs
            aesdecrypt = aesDecrypt(conn, rijndael, rijndael.mergeKeyIDs(), aescrypt);
            log.debug("Decrypt[{}]", new String(aesdecrypt));

            // Carico sul secondo mulo
            rijndael.load(module2);
            printKey(rijndael, module2);
            aesdecrypt = aesDecrypt(conn, rijndael, rijndael.getKeyID(module2), aescrypt);
            log.debug("Decrypt[{}]", new String(aesdecrypt));

            /*
            // Cifratura RSA
            Security.addProvider(new nCipherKM());
            //Signature dsa = Signature.getInstance("SHA256withDSA", Security.getProvider("nCipherKM"));
            KMRSAPrivateKey kPrivate = new KMRSAPrivateKey(rijndael);
            Signature dsa = Signature.getInstance("SHA1withRSA");
            dsa.initSign(kPrivate);
            dsa.update(datoDaCifrare.getBytes());
            byte[] sig = dsa.sign();

            KMRSAPublicKey kPublic = new KMRSAPublicKey(rijndael);
            dsa = Signature.getInstance("SHA1withRSA");
            dsa.initVerify(kPublic);
            log.debug("Check Decrypt[{}]", dsa.verify(sig));
            */
            // | NoSuchAlgorithmException | InvalidKeyException | SignatureException 
        } catch (NFException ex) {
            log.error("Exception", ex);
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

    private void printKeys(final SecurityWorld sw, final Module module) throws NFException {
        Key[] keys = sw.listKeys("simple");
        log.info("Keys:" + keys.length);
        Arrays.asList(keys).forEach(key -> {
            try {
                printKey(key, module);
            } catch (NFException nFException) {
                log.error("FException", nFException);
            }
        }
        );
    }

    private void printKey(final Key key, final Module module) throws NFException {
        log.info("Name[{}]"
                + " APP [{}]"
                + " Ident[{}]"
                + " Created[{}]"
                + " APP desc[{}]"
                + " KeyId[{}]"
                + " PublicKeyId[{}]"
                + " MergeKeyId[{}]"
                + " info [{}]"
                + " Extrainfo[{}]",
                key.getName(),
                key.getAppName(),
                key.getIdent(),
                key.getCreationDate(),
                key.getAppDescription(),
                key.getKeyID(module),
                key.getPublicKeyID(module),
                mapParameter(key.mergeKeyIDs()),
                key.toString(),
                key.getExtraInfo().trim());
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

    public byte[] aesDecrypt(final NFConnection conn, final Key key, final M_KeyID mKeyId, final byte[] ciphertext) throws NFException {
        M_Command cmd = new M_Command();
        cmd.cmd = M_Cmd.Decrypt;
        M_Cmd_Args_Decrypt args = new M_Cmd_Args_Decrypt();
        args.key = mKeyId;
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
