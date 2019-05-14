import org.apache.commons.codec.digest.DigestUtils;

public class Encrypter {
    public static String encrypt(String st){
        String md5Hex = DigestUtils.md5Hex(st);
        return md5Hex;
    }
}
