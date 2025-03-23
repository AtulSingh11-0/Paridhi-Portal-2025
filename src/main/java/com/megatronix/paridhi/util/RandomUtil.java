package com.megatronix.paridhi.util;


import java.security.SecureRandom;

public final class RandomUtil {
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private RandomUtil() {}

	public static SecureRandom getSecureRandom() {
		return SECURE_RANDOM;
	}

	public static int generateRandomInteger(int min, int max) {
		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}
		return min + SECURE_RANDOM.nextInt((max - min) + 1);
	}

	public static String generateOtp() {
		int otpNum = 100000 + SECURE_RANDOM.nextInt(900000); // 6-digit number between 100000 and 999999
		return String.valueOf(otpNum);
	}
}
