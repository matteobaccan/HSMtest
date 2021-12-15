package it.baccan.hsmtest;

import com.ncipher.jutils.HexFunctions;
import com.ncipher.km.nfkm.ConsoleCallBack;
import com.ncipher.km.nfkm.Key;
import com.ncipher.km.nfkm.KeyGenCallBack;
import com.ncipher.km.nfkm.KeyGenerator;
import com.ncipher.km.nfkm.SecurityWorld;
import com.ncipher.nfast.NFException;
import com.ncipher.nfast.connect.NFConnection;
import com.ncipher.nfast.marshall.M_Act_Details_OpPermissions;
import com.ncipher.nfast.marshall.M_ByteBlock;
import com.ncipher.nfast.marshall.M_CipherText;
import com.ncipher.nfast.marshall.M_Cmd;
import com.ncipher.nfast.marshall.M_Cmd_Args_Decrypt;
import com.ncipher.nfast.marshall.M_Cmd_Args_Encrypt;
import com.ncipher.nfast.marshall.M_Cmd_Args_Export;
import com.ncipher.nfast.marshall.M_Cmd_Args_GenerateKey;
import com.ncipher.nfast.marshall.M_Cmd_Reply_Decrypt;
import com.ncipher.nfast.marshall.M_Cmd_Reply_Encrypt;
import com.ncipher.nfast.marshall.M_Cmd_Reply_Export;
import com.ncipher.nfast.marshall.M_Command;
import com.ncipher.nfast.marshall.M_KeyGenParams;
import com.ncipher.nfast.marshall.M_KeyType;
import com.ncipher.nfast.marshall.M_KeyType_Data_Random;
import com.ncipher.nfast.marshall.M_KeyType_GenParams_Random;
import com.ncipher.nfast.marshall.M_Mech;
import com.ncipher.nfast.marshall.M_Mech_Cipher_Generic128;
import com.ncipher.nfast.marshall.M_PlainText;
import com.ncipher.nfast.marshall.M_PlainTextType;
import com.ncipher.nfast.marshall.M_PlainTextType_Data_Bytes;
import com.ncipher.nfast.marshall.M_Reply;
import com.ncipher.nfast.marshall.M_Status;

class ChangeACLCallback extends KeyGenCallBack {

    @Override
    public M_Command modifyGenerateKeyCmd(M_Command cmd) {
        // Add export permission
        ((M_Act_Details_OpPermissions) ((M_Cmd_Args_GenerateKey) cmd.args).acl.groups[0].actions[0].details).perms |= M_Act_Details_OpPermissions.perms_ExportAsPlain;
        return cmd;
    }
}

public class EncryptDecrypt {

    public static void main(String[] arguments) throws NFException {
        String plain = "stringa da cifrare";
        int modNumber = 2;

        // get world and connection
        SecurityWorld sw = new SecurityWorld(new ConsoleCallBack());
        NFConnection conn = sw.getConnection();
        // EasyConnection econn = new EasyConnection(conn);

        // create key
        KeyGenerator kg = sw.getKeyGenerator();
        kg.setKeyGenCallBack(new ChangeACLCallback());
        M_KeyGenParams params = new M_KeyGenParams(M_KeyType.Rijndael, new M_KeyType_GenParams_Random(32));
        Key key = kg.generateKey(params, "aeskey", "simple", "aeskey", null, sw.getModule(2), null, true);

        // create encryption command
        M_Command cmd = new M_Command();
        cmd.cmd = M_Cmd.Encrypt;

        // compile args
        M_Cmd_Args_Encrypt args = new M_Cmd_Args_Encrypt();
        args.key = key.getKeyID(sw.getModule(modNumber));
        args.mech = M_Mech.RijndaelmECBpPKCS5;
        args.plain = new M_PlainText();
        args.plain.type = M_PlainTextType.Bytes;
        args.plain.data = new M_PlainTextType_Data_Bytes();
        ((M_PlainTextType_Data_Bytes) args.plain.data).data = new M_ByteBlock(plain.getBytes());
        cmd.args = args;

        // Transact the command
        M_Reply rep = conn.transact(cmd);
        if (rep.status != M_Status.OK) {
            throw new NFException("Command failed");
        }
        byte[] crypted = ((M_Mech_Cipher_Generic128) ((M_Cmd_Reply_Encrypt) rep.reply).cipher.data).cipher.value;

        System.out.println("crypted succesfully! " + HexFunctions.byte2hex(crypted));

        // decrypt it
        M_Cmd_Args_Decrypt args1 = new M_Cmd_Args_Decrypt();
        args1.key = key.getKeyID(sw.getModule(modNumber));
        args1.mech = M_Mech.RijndaelmECBpPKCS5;
        args1.reply_type = M_PlainTextType.Bytes;
        args1.cipher = new M_CipherText();
        args1.cipher.mech = M_Mech.RijndaelmECBpPKCS5;
        args1.cipher.data = new M_Mech_Cipher_Generic128();
        ((M_Mech_Cipher_Generic128) args1.cipher.data).cipher = new M_ByteBlock(crypted);

        cmd.cmd = M_Cmd.Decrypt;
        cmd.args = args1;

        // Transact the command
        M_Reply rep1 = conn.transact(cmd);
        if (rep1.status != M_Status.OK) {
            throw new NFException("Command failed");
        }
        byte[] decrypted = ((M_PlainTextType_Data_Bytes) ((M_Cmd_Reply_Decrypt) rep1.reply).plain.data).data.value;

        System.out.println("decrypted succesfully! " + new String(decrypted) + "\n");

        // export the key
        cmd.cmd = M_Cmd.Export;
        M_Cmd_Args_Export args2 = new M_Cmd_Args_Export();
        args2.key = key.getKeyID(sw.getModule(modNumber));
        cmd.args = args2;
        // Transact the command
        M_Reply rep2 = conn.transact(cmd);
        if (rep2.status != M_Status.OK) {
            throw new NFException("Command failed");
        }

        byte[] theKey = ((M_KeyType_Data_Random) ((M_Cmd_Reply_Export) rep2.reply).data.data).k.value;

        String hex = HexFunctions.byte2hex(theKey);
        System.out.println("exported succesfully! " + hex);
    }
}
