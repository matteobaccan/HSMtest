package it.baccan.hsmtest;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import com.ncipher.km.nfkm.Key;
import com.ncipher.km.nfkm.Module;
import com.ncipher.nfast.NFException;
import com.ncipher.provider.km.KMRSAPrivateKey;
import com.ncipher.provider.km.KMRSAPublicKey;
import com.ncipher.provider.km.nCipherKM;

public class JCE {
	private Key key;

	public PrivateKey loadJCE(String ident) throws Exception {
		try {
			Module[] modules = nCipherKM.getSW().getModules();
			key = nCipherKM.getSW().getKey("simple", ident);
			if (key == null) {
				throw new Exception("Can't find the key");
			}
			
			// La carico in tutti i moduli
			Arrays.asList(modules).forEach(module -> {
				try {
					key.load(module);
					key.loadPublic(module);
				} catch (NFException e) {
					e.printStackTrace();
				}
			});
			
			return new KMRSAPrivateKey(key);
		} catch (NFException e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public PublicKey loadJCEPublic(String ident) throws Exception {
		try {
			Module[] modules = nCipherKM.getSW().getModules();
			key = nCipherKM.getSW().getKey("simple", ident);
			if (key == null) {
				throw new Exception("Can't find the key");
			}
			
			// La carico in tutti i moduli
			Arrays.asList(modules).forEach(module -> {
				try {
					key.loadPublic(module);
				} catch (NFException e) {
					e.printStackTrace();
				}
			});
			
			return new KMRSAPublicKey(key);
		} catch (NFException e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce rutrum sem quis blandit vulputate. Morbi feugiat orci enim, in aliquam tortor sollicitudin id. Duis eu ligula tincidunt, consectetur tortor ac, varius mauris. Donec consectetur massa augue, et euismod lacus suscipit id. Ut aliquam ac risus in consequat. Sed bibendum convallis porta. Pellentesque risus erat, posuere quis ligula commodo, consequat pharetra metus. Duis et dolor at tellus lobortis pellentesque. Phasellus imperdiet urna in metus ultricies, sed mollis urna imperdiet. Nunc pellentesque sapien sodales mauris faucibus elementum. Sed massa leo, dapibus sit amet nunc id, facilisis aliquam sapien. Vivamus sagittis metus at purus tristique bibendum. Ut semper sed eros quis accumsan. Donec quam massa, bibendum faucibus posuere et, imperdiet id felis. Nam eget lectus ac lectus pellentesque porta id ut erat. Fusce porta non nunc feugiat finibus.";
		
		JCE jce = new JCE();
		Sign sign = new Sign();
		try {
			PrivateKey key = jce.loadJCE("test202112151");
			PublicKey publicKey = jce.loadJCEPublic("test202112151");
			byte[] signature = sign.sign(nCipherKM.getConnection(), ((KMRSAPrivateKey)key).getKey(), text.getBytes());
			
			if (sign.verify(nCipherKM.getConnection(), ((KMRSAPrivateKey)key).getKey(), text.getBytes(), signature)) {
				System.out.println("OK");
			} else {
				System.out.println("Failed");
			}
			
			if (sign.verifyJCE((KMRSAPublicKey) publicKey, text.getBytes(), signature)) {
				System.out.println("OK");
			} else {
				System.out.println("Failed");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
