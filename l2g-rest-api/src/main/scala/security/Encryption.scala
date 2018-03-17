package security

import javax.crypto.{KeyGenerator, Cipher}
import javax.crypto.spec.SecretKeySpec

import org.apache.commons.codec.binary.Base64

trait EncryptionOps {
  def encrypt(dataBytes: Array[Byte], secret: String): Array[Byte]
  def decrypt(codeBytes: Array[Byte], secret: String): Array[Byte]
}

object DES extends Encryption("DES")
object AES extends Encryption("AES")

class Encryption(algorithm: String) extends EncryptionOps {
  private def decodeBase64(string: String) = Base64.decodeBase64(string)
  private def cipher(mode: Int, b64secret: String): Cipher = {
    val encipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding")
    encipher.init(mode, new SecretKeySpec(decodeBase64(b64secret), algorithm))
    encipher
  }

  def encrypt(bytes: Array[Byte], secretKey: String): Array[Byte] = {
    val encoder = cipher(Cipher.ENCRYPT_MODE, secretKey)
    encoder.doFinal(bytes)
  }

  def decrypt(bytes: Array[Byte], secretKey: String): Array[Byte] = {
    val decoder = cipher(Cipher.DECRYPT_MODE, secretKey)
    decoder.doFinal(bytes)
  }

  def encrypt(text: String, secretKey: String): String = Base64.encodeBase64String(encrypt(text.getBytes("UTF-8"), secretKey))

  def decrypt(encrypted: String, secretKey: String): String = {
    new String(decrypt(Base64.decodeBase64(encrypted), secretKey), "UTF-8")
  }
}

object EncryptionUtils {
  def main(args: Array[String]) {
    val key = Base64.encodeBase64String(generateKey("AES", 128))
    println(key)
  }

  def generateKey(algorithm: String, size: Int): Array[Byte] = {
    val generator = KeyGenerator.getInstance(algorithm)
    generator.init(size)
    generator.generateKey().getEncoded
  }

}