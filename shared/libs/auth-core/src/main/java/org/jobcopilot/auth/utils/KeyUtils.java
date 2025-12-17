package org.jobcopilot.auth.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@UtilityClass
public class KeyUtils {

  /** Load RSA private key from a PKCS#12 keystore file. */
  public PrivateKey getPrivateKeyFromPkcs12(String p12Path, String alias, char[] password)
      throws IOException, GeneralSecurityException {
    try (var inputStream = Files.newInputStream(Path.of(p12Path))) {
      var keyStore = KeyStore.getInstance("PKCS12");
      keyStore.load(inputStream, password);
      var key = keyStore.getKey(alias, password);
      if (key instanceof PrivateKey privateKey) {
        return privateKey;
      }
      throw new GeneralSecurityException("No private key found for alias: " + alias);
    }
  }

  /** Load RSA public key from a PEM file. */
  public PublicKey getPublicKeyFromPem(String pemPath) throws IOException, GeneralSecurityException {
    String pem = Files.readString(Path.of(pemPath));
    byte[] keyBytes = decodePem(pem, "PUBLIC KEY");
    var spec = new X509EncodedKeySpec(keyBytes);
    return KeyFactory.getInstance("RSA").generatePublic(spec);
  }

  private byte[] decodePem(String pem, String type) {
    String normalized =
        pem
            .replace("-----BEGIN " + type + "-----", "")
            .replace("-----END " + type + "-----", "")
            .replaceAll("\\s", "");
    return Base64.getDecoder().decode(normalized);
  }
}
