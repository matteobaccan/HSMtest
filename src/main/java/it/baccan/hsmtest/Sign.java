package it.baccan.hsmtest;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;

import com.ncipher.km.nfkm.Key;
import com.ncipher.nfast.NFException;
import com.ncipher.nfast.connect.NFConnection;
import com.ncipher.nfast.marshall.M_Bignum;
import com.ncipher.nfast.marshall.M_ByteBlock;
import com.ncipher.nfast.marshall.M_CipherText;
import com.ncipher.nfast.marshall.M_Cmd;
import com.ncipher.nfast.marshall.M_Cmd_Args_Sign;
import com.ncipher.nfast.marshall.M_Cmd_Args_Verify;
import com.ncipher.nfast.marshall.M_Cmd_Reply_Sign;
import com.ncipher.nfast.marshall.M_Command;
import com.ncipher.nfast.marshall.M_Mech;
import com.ncipher.nfast.marshall.M_Mech_Cipher_RSApPKCS1;
import com.ncipher.nfast.marshall.M_PlainText;
import com.ncipher.nfast.marshall.M_PlainTextType;
import com.ncipher.nfast.marshall.M_PlainTextType_Data_Bytes;
import com.ncipher.nfast.marshall.M_Reply;
import com.ncipher.nfast.marshall.M_Status;
import com.ncipher.provider.km.nCipherKM;

public class Sign {
	
	public byte[] signJCE(PrivateKey key, byte[] plain) throws Exception {
		System.setProperty("protect", "module");

		Provider securityProvider = Security.getProvider("nCipherKM");
		if (securityProvider == null) {
			Security.addProvider(new nCipherKM());
			securityProvider = Security.getProvider("nCipherKM");
		}
		
		try {
			Signature sig = Signature.getInstance("SHA256withRSA", securityProvider);
			sig.initSign(key);
			sig.update(plain);
			return sig.sign();
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public boolean verifyJCE(PublicKey key, byte[] plain, byte[] signature) throws Exception {
		System.setProperty("protect", "module");

		Provider securityProvider = Security.getProvider("nCipherKM");
		if (securityProvider == null) {
			Security.addProvider(new nCipherKM());
			securityProvider = Security.getProvider("nCipherKM");
		}
		
		try {
			Signature sig = Signature.getInstance("SHA256withRSA", securityProvider);
			sig.initVerify(key);
//			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
//			sig.update(messageDigest.digest(plain));
			sig.update(plain);
			if (!sig.verify(signature)) 
				throw new Exception("Verify failed");
			return true;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public byte[] sign(NFConnection conn, Key key, byte[] plain) throws NFException {
		M_Command cmd = new M_Command();
		cmd.cmd = M_Cmd.Sign;
		M_Cmd_Args_Sign args = new M_Cmd_Args_Sign();
		args.key = key.mergeKeyIDs();
		args.mech = M_Mech.RSAhSHA256pPKCS1; //.RSAhSHA256pPSS;
		args.plain = new M_PlainText();
		args.plain.type = M_PlainTextType.Bytes;
		args.plain.data = new M_PlainTextType_Data_Bytes();
		((M_PlainTextType_Data_Bytes) args.plain.data).data = new M_ByteBlock(plain);
		cmd.args = args;
		// Transact the command
		M_Reply rep = conn.transact(cmd);
		if (rep.status != M_Status.OK) {
			throw new NFException("Command failed");
		}
		return ((M_Mech_Cipher_RSApPKCS1) ((M_Cmd_Reply_Sign) rep.reply).sig.data).m.value.toByteArray();
	}

	public boolean verify(NFConnection conn, Key key, byte[] plain, byte[] signature) throws NFException {
		M_Command cmd = new M_Command();
		cmd.cmd = M_Cmd.Verify;
		M_Cmd_Args_Verify args = new M_Cmd_Args_Verify();
		args.key = key.mergePublic();
		args.mech = M_Mech.RSAhSHA256pPKCS1; //RSAhSHA256pPSS;
		args.plain = new M_PlainText();
		args.plain.type = M_PlainTextType.Bytes;
		args.plain.data = new M_PlainTextType_Data_Bytes();
		((M_PlainTextType_Data_Bytes) args.plain.data).data = new M_ByteBlock(plain);
		args.sig = new M_CipherText();
		args.sig.mech = M_Mech.RSAhSHA256pPKCS1; //RSAhSHA256pPSS;
		args.sig.data = new M_Mech_Cipher_RSApPKCS1(new M_Bignum(new BigInteger(signature)));
		cmd.args = args;
		// Transact the command
		M_Reply rep = conn.transact(cmd);
		if (rep.status != M_Status.OK) {
			throw new NFException("Command failed");
		}
		if (rep.status == M_Status.VerifyFailed) {
			return false;
		}
		return true;
	}
}
