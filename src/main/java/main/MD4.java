
package main;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Реализация MD4 Message Digest
 */
public class MD4 extends MessageDigest implements Cloneable {
    /**
     * The size in bytes of the input block to the tranformation algorithm.
     */
    private static final int BLOCK_LENGTH = 64;       //    = 512 / 8;
    /**
     * 4 32-bit words (interim result) (A,B,C,D)
     */
    private int[] context = new int[4];
    /**
     * Number of bytes processed so far mod. 2 power of 64.
     */
    private long count;
    /**
     * 512 bits input buffer = 16 x 32-bit words holds until reaches 512 bits.
     */
    private byte[] buffer = new byte[BLOCK_LENGTH];
    /**
     * 512 bits work buffer = 16 x 32-bit words
     */
    private int[] X = new int[16];

    public MD4() {
        super("MD4");
        engineReset();
    }

    private MD4(MD4 md) {
        this();
        context = md.context.clone();
        buffer = md.buffer.clone();
        count = md.count;
    }

    public String hash(String text) {
        update(text.getBytes());
        BigInteger hash = new BigInteger(1, digest());
        return hash.toString(16);
    }

    /**
     * Returns a copy of this MD object.
     */
    public Object clone() {
        return new MD4(this);
    }

    /**
     * Resets this object disregarding any temporary data present at the time of
     * the invocation of this call.
     */
    @Override
    public void engineReset() {
        context[0] = 0x67452301;
        context[1] = 0xEFCDAB89;
        context[2] = 0x98BADCFE;
        context[3] = 0x10325476;
        count = 0L;
        for (int i = 0; i < BLOCK_LENGTH; i++) {
            buffer[i] = 0;
        }
    }

    /**
     * Continues an MD4 message digest using the input byte.
     */
    @Override
    public void engineUpdate(byte b) {
        // подсчитываем количество всё еще не захешированных байт
        int i = (int) (count % BLOCK_LENGTH);
        count++;                                        // обновляем номер байта
        buffer[i] = b;
        if (i == BLOCK_LENGTH - 1) {
            transform(buffer, 0);
        }
    }

    /**
     * MD4 block update operation. <p> Continues an MD4 message digest
     * operation, by filling the buffer, transform(ing) data in 512-bit message
     * block(s), updating the variables context and count, and leaving
     * (buffering) the remaining bytes in buffer for the next update or finish.
     *
     * @param input  input block
     * @param offset start of meaningful bytes in input
     * @param len    count of bytes in input block to consider
     */
    @Override
    public void engineUpdate(byte[] input, int offset, int len) {
        if (offset < 0 || len < 0 || (long) offset + len > input.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        // подсчитываем количество всё еще не захешированных байт
        int bufferNdx = (int) (count % BLOCK_LENGTH);
        count += len;
        int partLen = BLOCK_LENGTH - bufferNdx;
        int i = 0;
        if (len >= partLen) {
            System.arraycopy(input, offset, buffer, bufferNdx, partLen);
            transform(buffer, 0);
            for (i = partLen; i + BLOCK_LENGTH - 1 < len; i += BLOCK_LENGTH) {
                transform(input, offset + i);
            }
            bufferNdx = 0;
        }
        if (i < len) {
            System.arraycopy(input, offset + i, buffer, bufferNdx, len - i);
        }
    }

    /**
     * Completes the hash computation by performing final operations such as
     * padding. At the return of this engineDigest, the MD engine is reset.
     *
     * @return the array of bytes for the resulting hash value.
     */
    @Override
    public byte[] engineDigest() {
        // Поиск количество дополнения до длинны, по модулю 512 равной 448
        int bufferNdx = (int) (count % BLOCK_LENGTH);
        int padLen = (bufferNdx < 56) ? (56 - bufferNdx) : (120 - bufferNdx);

        // первый бит 1, остальные - 0
        byte[] tail = new byte[padLen + 8];
        tail[0] = (byte) 0x80;

        // добавляем исходную длинну
        // сначала младший байты
        for (int i = 0; i < 8; i++) {
            tail[padLen + i] = (byte) ((count * 8) >>> (8 * i));
        }

        engineUpdate(tail, 0, tail.length);

        byte[] result = new byte[16];
        // преобразуем результат из массива 4х int в массив из 16 байт
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i * 4 + j] = (byte) (context[i] >>> (8 * j));
            }
        }

        engineReset();
        return result;
    }

    /**
     * MD4 basic transformation. <p> Transforms context based on 512 bits from
     * input block starting from the offset'th byte.
     *
     * @param block  input sub-array.
     * @param offset starting position of sub-array.
     */
    private void transform(byte[] block, int offset) {
        // переводим массив из 64 байт из входного блока в массив из 16 блоков по 32 бита
        for (int i = 0; i < 16; i++) {
            X[i] = (block[offset++] & 0xFF)
                    | (block[offset++] & 0xFF) << 8
                    | (block[offset++] & 0xFF) << 16
                    | (block[offset++] & 0xFF) << 24;
        }
        int A = context[0];
        int B = context[1];
        int C = context[2];
        int D = context[3];
        A = FF(A, B, C, D, X[0], 3);
        D = FF(D, A, B, C, X[1], 7);
        C = FF(C, D, A, B, X[2], 11);
        B = FF(B, C, D, A, X[3], 19);
        A = FF(A, B, C, D, X[4], 3);
        D = FF(D, A, B, C, X[5], 7);
        C = FF(C, D, A, B, X[6], 11);
        B = FF(B, C, D, A, X[7], 19);
        A = FF(A, B, C, D, X[8], 3);
        D = FF(D, A, B, C, X[9], 7);
        C = FF(C, D, A, B, X[10], 11);
        B = FF(B, C, D, A, X[11], 19);
        A = FF(A, B, C, D, X[12], 3);
        D = FF(D, A, B, C, X[13], 7);
        C = FF(C, D, A, B, X[14], 11);
        B = FF(B, C, D, A, X[15], 19);

        A = GG(A, B, C, D, X[0], 3);
        D = GG(D, A, B, C, X[4], 5);
        C = GG(C, D, A, B, X[8], 9);
        B = GG(B, C, D, A, X[12], 13);
        A = GG(A, B, C, D, X[1], 3);
        D = GG(D, A, B, C, X[5], 5);
        C = GG(C, D, A, B, X[9], 9);
        B = GG(B, C, D, A, X[13], 13);
        A = GG(A, B, C, D, X[2], 3);
        D = GG(D, A, B, C, X[6], 5);
        C = GG(C, D, A, B, X[10], 9);
        B = GG(B, C, D, A, X[14], 13);
        A = GG(A, B, C, D, X[3], 3);
        D = GG(D, A, B, C, X[7], 5);
        C = GG(C, D, A, B, X[11], 9);
        B = GG(B, C, D, A, X[15], 13);

        A = HH(A, B, C, D, X[0], 3);
        D = HH(D, A, B, C, X[8], 9);
        C = HH(C, D, A, B, X[4], 11);
        B = HH(B, C, D, A, X[12], 15);
        A = HH(A, B, C, D, X[2], 3);
        D = HH(D, A, B, C, X[10], 9);
        C = HH(C, D, A, B, X[6], 11);
        B = HH(B, C, D, A, X[14], 15);
        A = HH(A, B, C, D, X[1], 3);
        D = HH(D, A, B, C, X[9], 9);
        C = HH(C, D, A, B, X[5], 11);
        B = HH(B, C, D, A, X[13], 15);
        A = HH(A, B, C, D, X[3], 3);
        D = HH(D, A, B, C, X[11], 9);
        C = HH(C, D, A, B, X[7], 11);
        B = HH(B, C, D, A, X[15], 15);

        context[0] += A;
        context[1] += B;
        context[2] += C;
        context[3] += D;
    }

    private int FF(int a, int b, int c, int d, int x, int s) {
        int t = a + ((b & c) | (~b & d)) + x;
        return t << s | t >>> (32 - s);
    }

    private int GG(int a, int b, int c, int d, int x, int s) {
        int t = a + ((b & (c | d)) | (c & d)) + x + 0x5A827999;
        return t << s | t >>> (32 - s);
    }

    private int HH(int a, int b, int c, int d, int x, int s) {
        int t = a + (b ^ c ^ d) + x + 0x6ED9EBA1;
        return t << s | t >>> (32 - s);
    }
}
