package com.demo.upimesh.crypto;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;

/**
 * RSA-PSS digital signatures for sender authentication.
 *
 * Why RSA-PSS instead of PKCS#1 v1.5? PSS is provably secure, randomized,
 * and the modern standard (RFC 8017). It prevents deterministic signature
 * forgery attacks that PKCS#1 v1.5 is vulnerable to.
 *
 * Each device generates its own 2048-bit RSA keypair. The private key never
 * leaves the device. The public key is registered with the settlement gateway
 * and linked to the device's VPA.
 */
@Service
public class DigitalSignatureService {

    private static final String ALGORITHM = "RSASSA-PSS";
    private static final int RSA_KEY_BITS = 2048;
    private static final int SALT_LEN = 32; // bytes

    private final SecureRandom rng = new SecureRandom();

    /**
     * Generate a fresh 2048-bit RSA keypair for a device.
     */
    public KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(RSA_KEY_BITS, rng);
        return gen.generateKeyPair();
    }

    /**
     * Sign canonical data with a device private key.
     * Returns base64-encoded signature.
     */
    public String sign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature sig = Signature.getInstance(ALGORITHM);
        PSSParameterSpec pss = new PSSParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, SALT_LEN, 1);
        sig.setParameter(pss);
        sig.initSign(privateKey);
        sig.update(data);
        return Base64.getEncoder().encodeToString(sig.sign());
    }

    /**
     * Verify a base64 signature against canonical data and a public key.
     */
    public boolean verify(byte[] data, String signatureBase64, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance(ALGORITHM);
        PSSParameterSpec pss = new PSSParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, SALT_LEN, 1);
        sig.setParameter(pss);
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(Base64.getDecoder().decode(signatureBase64));
    }

    /**
     * SHA-256 fingerprint of a public key. Used as the lookup key in the DB.
     */
    public String fingerprint(PublicKey publicKey) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(publicKey.getEncoded());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    /**
     * Encode a public key to base64 for storage.
     */
    public String encodePublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Decode a base64 public key.
     */
    public PublicKey decodePublicKey(String base64) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64);
        java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}