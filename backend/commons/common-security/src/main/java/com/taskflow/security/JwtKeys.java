package com.taskflow.security;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class JwtKeys {
    private JwtKeys() { }

    private static byte[] decode(String pem) {
        return Base64.getDecoder().decode(pem.replaceAll("-----[^-]+-----", "").replaceAll("\\s", ""));
    }

    public static RSAPublicKey publicKey(String pem) {
        try {
            var key = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decode(pem)));
            if (key.getModulus().bitLength() < 3072) throw new IllegalArgumentException("RSA key is too short");
            return key;
        } catch (Exception exception) {
            throw new IllegalArgumentException("A valid RSA public key of at least 3072 bits is required");
        }
    }

    public static RSAPrivateKey privateKey(String pem) {
        try {
            var key = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decode(pem)));
            if (key.getModulus().bitLength() < 3072) throw new IllegalArgumentException("RSA key is too short");
            return key;
        } catch (Exception exception) {
            throw new IllegalArgumentException("A valid PKCS#8 RSA private key of at least 3072 bits is required");
        }
    }
}
