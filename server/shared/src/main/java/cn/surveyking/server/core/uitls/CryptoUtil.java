package cn.surveyking.server.core.uitls;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 加解密工具（PRD-04 PII 加密）。
 * 密钥由口令经 PBKDF2 派生，IV 随机 12 字节，密文格式 base64(iv + ciphertext)。
 * 认证失败（篡改）抛异常，不静默返回空。
 *
 * @author eng-koudouma
 */
public class CryptoUtil {

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final int IV_LENGTH = 12;

	private static final int TAG_BITS = 128;

	private static final int SALT_LENGTH = 16;

	private static final int ITERATIONS = 10000;

	private static final int KEY_BITS = 256;

	/**
	 * 使用密钥口令加密
	 * 
	 * @param passphrase 密钥口令（sk.encrypt.key），不可为空
	 * @param plain      明文
	 * @return base64(iv + ciphertext)
	 */
	public static String encrypt(String passphrase, String plain) {
		if (plain == null) {
			return null;
		}
		try {
			byte[] salt = new byte[SALT_LENGTH];
			RANDOM.nextBytes(salt);
			SecretKey key = deriveKey(passphrase, salt);
			byte[] iv = new byte[IV_LENGTH];
			RANDOM.nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
			byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
			byte[] out = new byte[salt.length + iv.length + cipherText.length];
			System.arraycopy(salt, 0, out, 0, salt.length);
			System.arraycopy(iv, 0, out, salt.length, iv.length);
			System.arraycopy(cipherText, 0, out, salt.length + iv.length, cipherText.length);
			return Base64.getEncoder().encodeToString(out);
		}
		catch (Exception ex) {
			throw new IllegalStateException("加密失败", ex);
		}
	}

	/**
	 * 使用密钥口令解密；密文非法/篡改抛异常
	 * 
	 * @param passphrase 密钥口令
	 * @param encrypted  base64(iv + ciphertext)
	 * @return 明文
	 */
	public static String decrypt(String passphrase, String encrypted) {
		if (encrypted == null || encrypted.isEmpty()) {
			return null;
		}
		try {
			byte[] all = Base64.getDecoder().decode(encrypted);
			byte[] salt = new byte[SALT_LENGTH];
			byte[] iv = new byte[IV_LENGTH];
			byte[] cipherText = new byte[all.length - salt.length - iv.length];
			System.arraycopy(all, 0, salt, 0, salt.length);
			System.arraycopy(all, salt.length, iv, 0, iv.length);
			System.arraycopy(all, salt.length + iv.length, cipherText, 0, cipherText.length);
			SecretKey key = deriveKey(passphrase, salt);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
			return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
		}
		catch (Exception ex) {
			throw new IllegalStateException("解密失败，密文可能被篡改或密钥错误", ex);
		}
	}

	private static SecretKey deriveKey(String passphrase, byte[] salt) throws Exception {
		PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_BITS);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
	}

}
