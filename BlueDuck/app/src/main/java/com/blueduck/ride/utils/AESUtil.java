/**
 * FileName:     AESUtil.java
 * @Description: TODO
 * All rights Reserved, Designed By OMNI.COM  
 * Copyright:    Copyright(C) 2012-2017
 * Company       OMNI
 * @author:    Albert
 * @version    V1.0 
 * Createdate:         2017年11月10日 下午2:20:59
 *
 * Modification  History:
 * Date         Author        Version        Discription
 * -----------------------------------------------------------------------------------
 * 2017年11月10日       CQCN         1.0             1.0
 * Why & What is modified: <初始化>
 */
package com.blueduck.ride.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @ClassName:     AESUtil
 * @Description:TODO
 * @author:    Albert
 * @date:        2017年11月10日 下午2:20:59
 *
 */
public class AESUtil {
	public static final String VIPARA = "D1B6547519523C84";//加密初始向量 Encrypted initial vector
	public static final String PHONE_KEY = "37E916B299213D83";//加密密匙 Encryption key
	public static final String bm = "utf-8";

	/**
	 * Convert byte array to uppercase hex string
	 * 字节数组转化为大写16进制字符串 
	 * @param b
	 * @return 
	 */  
	private static String byte2HexStr(byte[] b) {  
		StringBuilder sb = new StringBuilder();  
		for (int i = 0; i < b.length; i++) {  
			String s = Integer.toHexString(b[i] & 0xFF);  
			if (s.length() == 1) {  
				sb.append("0");  
			}
			sb.append(s.toUpperCase());  
		}
		return sb.toString();  
	}  

	/**
	 * Hexadecimal string to byte array
	 * 16进制字符串转字节数组 
	 * @param s
	 * @return 
	 */  
	private static byte[] str2ByteArray(String s) {  
		int byteArrayLength = s.length() / 2;  
		byte[] b = new byte[byteArrayLength];  
		for (int i = 0; i < byteArrayLength; i++) {  
			byte b0 = (byte) Integer.valueOf(s.substring(i * 2, i * 2 + 2), 16)  
					.intValue();  
			b[i] = b0;  
		}
		return b;  
	}  

	/**
	 * AES encryption
	 * AES 加密
	 * @param content
	 * @param password
	 * @return
	 */  

	public static String aesEncrypt(String content, String password) {  
		try {  
			IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes());  
			SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");  
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");  
			cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);  
			byte[] encryptedData = cipher.doFinal(content.getBytes(bm));  

			return Base64.encode(encryptedData);  
			//	          return byte2HexStr(encryptedData);  
		} catch (NoSuchAlgorithmException e) {  
			e.printStackTrace();  
		} catch (NoSuchPaddingException e) {  
			e.printStackTrace();  
		} catch (UnsupportedEncodingException e) {  
			e.printStackTrace();  
		} catch (InvalidKeyException e) {  
			e.printStackTrace();  
		} catch (IllegalBlockSizeException e) {  
			e.printStackTrace();  
		} catch (BadPaddingException e) {  
			e.printStackTrace();  
		} catch (InvalidAlgorithmParameterException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
		}
		return null;  
	}  

	/**
	 * AES decryption
	 * AES 解密 
	 * @param content
	 * @param password
	 * @return
	 */  

	public static String aesDecrypt(String content, String password) {  
		try {  
			byte[] byteMi = Base64.decode(content);  
			//	          byte[] byteMi=  str2ByteArray(content);  
			IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes());  
			SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");  
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");  
			cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);  
			byte[] decryptedData = cipher.doFinal(byteMi);  
			return new String(decryptedData, "utf-8");  
		} catch (NoSuchAlgorithmException e) {  
			e.printStackTrace();  
		} catch (NoSuchPaddingException e) {  
			e.printStackTrace();  
		} catch (InvalidKeyException e) {  
			e.printStackTrace();  
		} catch (IllegalBlockSizeException e) {  
			e.printStackTrace();  
		} catch (BadPaddingException e) {  
			e.printStackTrace();  
		} catch (UnsupportedEncodingException e) {  
			e.printStackTrace();  
		} catch (InvalidAlgorithmParameterException e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
		}  
		return null;  
	} 

	public static void main(String[] args) {
		String content="thisistestx";  
		String password="1259632153695615";  
		String jiamiresult=AESUtil.aesEncrypt(content, password);   
		System.out.println("加密结果"+jiamiresult);
		String jiemi=AESUtil.aesDecrypt(jiamiresult, password);
		System.out.println("解密结果"+jiemi);
	}
}
