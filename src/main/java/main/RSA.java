
package main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sasha
 */
public class RSA {
    private static final int bitLength = 1024;

    public static byte[] encrypt(byte[] text, BigInteger n, BigInteger e) {
        List<Byte> ab = new ArrayList<>();
        int size = bitLength / 8;
        int count_bytes = text.length;
        int count_blocks = count_bytes / size;
        int count_left = count_bytes % size;
        int count = count_blocks;
        if (count_left > 0) {
            count++;
        }
        for (int COUNT = 0; COUNT < count; COUNT++) {
            byte[] b = new byte[size];
            for (int i = 0; i < size; i++) {
                if ((i + COUNT * size) > text.length - 1) {
                    break;
                }
                b[i] = text[i + COUNT * size];
            }
            BigInteger bgg = new BigInteger(b);
            BigInteger b1 = bgg.modPow(e, n);
            byte[] barr = b1.toByteArray();
            for (final byte value : barr) {
                ab.add(value);
            }
        }
        byte[] br = new byte[ab.size()];
        for (int i = 0; i < ab.size(); i++) {
            br[i] = ab.get(i);
        }
        return br;
    }

    public static byte[] decrypt(byte[] text, BigInteger n, BigInteger key) {
        List<Byte> ab = new ArrayList<>();
        int size = bitLength / 8;
        int count_bytes = text.length;
        int count_blocks = count_bytes / size;
        int count_left = count_bytes % size;
        int count = count_blocks;
        if (count_left > 0) {
            count++;
        }
        for (int COUNT = 0; COUNT < count; COUNT++) {
            byte[] b = new byte[size];
            for (int i = 0; i < size; i++) {
                if ((i + COUNT * size) > text.length - 1) {
                    break;
                }
                b[i] = text[i + COUNT * size];
            }
            BigInteger b1 = new BigInteger(b);
            BigInteger b2 = b1.modPow(key, n);
            byte[] barr = b2.toByteArray();
            for (byte value : barr) {
                ab.add(value);
            }
        }
        byte[] br = new byte[ab.size()];
        for (int i = 0; i < ab.size(); i++) {
            br[i] = ab.get(i);
        }
        return br;
    }
}
