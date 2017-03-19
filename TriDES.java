package preapp;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TriDES {
	private static final String Algorithm = "DESede";

	/**
	 *  加密方法：
	 *  @param keybyte  密钥 二进制数组
	 *  @param src      待加密的明文
	 * */
	public static String encryptMode(byte[] keybyte, String src) {
		try
		{
			SecretKey deskey = new SecretKeySpec (keybyte, Algorithm);
			Cipher c1 = Cipher.getInstance(Algorithm+ "/ECB/PKCS5Padding" );
			c1.init (Cipher.ENCRYPT_MODE, deskey);
			return byte2Hex(c1.doFinal(src.getBytes()));
		}
		catch (Exception e) {
			e.printStackTrace(); 
		}
		return null;
	}

	
	/**
	 *  解密方法：
	 *  @param keybyte  密钥 二进制数组
	 *  @param src      待解密的明文
	 * */
	public static String decryptMode(byte[] keybyte, String src) {
		try {
			SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);
			Cipher c1 = Cipher.getInstance(Algorithm+ "/ECB/PKCS5Padding" );
			c1.init(Cipher.DECRYPT_MODE, deskey);
			return new String(c1.doFinal(hex2Byte(src)));
		}

		catch (Exception e) {
			e.printStackTrace(); 
		}
		return null;
	}

	public static String byte2Hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length () == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}

	public static byte[] hex2Byte (String str) {
		if (str == null)
			return null;
		str = str.trim();
		int len = str.length();
		if (len == 0 || len % 2 == 1)
			return null;
		byte[] b = new byte[len / 2];
		try {
			for (int i = 0; i < str.length(); i += 2) {
				b[i / 2] = (byte) Integer.decode("0x" + str.substring(i, i + 2)).intValue();
			}
			return b;
		} catch (Exception e) {
			return null;
		}
	}

}