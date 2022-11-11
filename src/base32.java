// I do not take any credit for this piece of code.
// This code was aquired from a github page some time ago.
// The gitbub page either changed it's location or it doesn't exist anymore.
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class base32 {

    private static final int PLAIN_DATA_BLOCK_SIZE = 5;
    private static final int ENCODED_DATA_BLOCK_SIZE = 8;

    private static final char PAD = '=';

    private static final byte[] TABLE_ENCODE = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', '2', '3', '4', '5', '6', '7',
    };

    private static final byte[] TABLE_ENCODE_EXTENDED_HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
    };

    private static final int[] TABLE_DECODE = new int[1 << 7];
    private static final int[] TABLE_DECODE_EXTENDED_HEX = new int[1 << 7];

    static {
        // Initialize
        Arrays.fill(TABLE_DECODE, -1);
        Arrays.fill(TABLE_DECODE_EXTENDED_HEX, -1);

        // build reverse lookup tables
        for (int i = 0; i < TABLE_ENCODE.length; i++) {
            TABLE_DECODE[TABLE_ENCODE[i]] = i;
            TABLE_DECODE_EXTENDED_HEX[TABLE_ENCODE_EXTENDED_HEX[i]] = i;
        }
    }

    public static String encode(byte[] input) {
        return Encoder.encode(input, TABLE_ENCODE);
    }

    public static byte[] decode(String input) {
        return Decoder.decode(input, TABLE_DECODE);
    }

    public static String encodeExtendedHex(byte[] input) {
        return Encoder.encode(input, TABLE_ENCODE_EXTENDED_HEX);
    }

    public static byte[] decodeExtendedHex(String input) {
        return Decoder.decode(input, TABLE_DECODE_EXTENDED_HEX);
    }

    private static class Encoder {
        private static final int BIT_WIDTH = 5;
        private static final long BIT_MASK = 0x1F; // = 00011111

        public static String encode(byte[] input, byte[] tableEncode) {
            if (input == null) {
                throw new IllegalArgumentException("Input data must not be null.");
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(input);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            encode(bis, bos, tableEncode);

            try {
                return bos.toString(StandardCharsets.US_ASCII.name());
            } catch (UnsupportedEncodingException e) {
                return bos.toString();
            }
        }

        private static void encode(
                ByteArrayInputStream byteArrayInputStream,
                ByteArrayOutputStream byteArrayOutputStream,
                byte[] tableEncode) {
            byte[] plainDataBlock = new byte[PLAIN_DATA_BLOCK_SIZE];
            byte[] encodedDataBlock = new byte[ENCODED_DATA_BLOCK_SIZE];

            int len;
            while ((len = byteArrayInputStream.read(plainDataBlock, 0, PLAIN_DATA_BLOCK_SIZE)) > 0) {
                int resultBlockSizeInBit = len * 8;
                int resultBlockSize = resultBlockSizeInBit / BIT_WIDTH + (resultBlockSizeInBit % BIT_WIDTH > 0 ? 1 : 0);
                int padSize = ENCODED_DATA_BLOCK_SIZE - resultBlockSize;

                long value = getLongFromBlock(plainDataBlock);

                encodedDataBlock[0] = tableEncode[getIndex(value, 35)];
                encodedDataBlock[1] = tableEncode[getIndex(value, 30)];
                encodedDataBlock[2] = tableEncode[getIndex(value, 25)];
                encodedDataBlock[3] = tableEncode[getIndex(value, 20)];
                encodedDataBlock[4] = tableEncode[getIndex(value, 15)];
                encodedDataBlock[5] = tableEncode[getIndex(value, 10)];
                encodedDataBlock[6] = tableEncode[getIndex(value, 5)];
                encodedDataBlock[7] = tableEncode[getIndex(value, 0)];

                byteArrayOutputStream.write(encodedDataBlock, 0, ENCODED_DATA_BLOCK_SIZE - padSize);

                for (int i = 0; i < padSize; i++) {
                    byteArrayOutputStream.write((byte) PAD);
                }

                // Clear plainDataBlock.
                Arrays.fill(plainDataBlock, (byte) 0);
            }
        }

        private static byte getIndex(long value, int shift) {
            return (byte) ((value & BIT_MASK << shift) >>> shift);
        }

        private static long getLongFromBlock(byte[] block) {
            return (byteToLong(block[0]) << 32)
                    + (byteToLong(block[1]) << 24)
                    + (byteToLong(block[2]) << 16)
                    + (byteToLong(block[3]) << 8)
                    + (byteToLong(block[4]));
        }

        private static long byteToLong(byte bucketValue) {
            return ((long) bucketValue) & 0xFF;
        }
    }

    private static class Decoder {

        public static byte[] decode(String input, int[] tableDecode) {
            if (input == null) {
                throw new IllegalArgumentException("Input string must not be null.");
            }
            if (input.length() == 0) {
                return new byte[0];
            }
            if (input.length() % 8 != 0) {
                throw new IllegalArgumentException("Input string length must be divisible by 8.");
            }

            int padSize = 0;
            if (input.indexOf(PAD) >= 0) {
                padSize = input.length() - input.indexOf(PAD);
            }

            String padStrippedStr = input.substring(0, input.length() - padSize);

            ByteArrayInputStream bis = new ByteArrayInputStream(padStrippedStr.getBytes(StandardCharsets.US_ASCII));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            decode(bis, bos, tableDecode);

            return bos.toByteArray();
        }

        private static void decode(
                ByteArrayInputStream byteArrayInputStream,
                ByteArrayOutputStream byteArrayOutputStream,
                int[] tableDecode) {
            byte[] plainDataBlock = new byte[PLAIN_DATA_BLOCK_SIZE];
            byte[] encodedDataBlock = new byte[ENCODED_DATA_BLOCK_SIZE];

            int len;
            while ((len = byteArrayInputStream.read(encodedDataBlock, 0, ENCODED_DATA_BLOCK_SIZE)) > 0) {
                long bucketValue0 = getTableValue(tableDecode, encodedDataBlock[0]);
                long bucketValue1 = getTableValue(tableDecode, encodedDataBlock[1]);
                long bucketValue2 = getTableValue(tableDecode, encodedDataBlock[2]);
                long bucketValue3 = getTableValue(tableDecode, encodedDataBlock[3]);
                long bucketValue4 = getTableValue(tableDecode, encodedDataBlock[4]);
                long bucketValue5 = getTableValue(tableDecode, encodedDataBlock[5]);
                long bucketValue6 = getTableValue(tableDecode, encodedDataBlock[6]);
                long bucketValue7 = getTableValue(tableDecode, encodedDataBlock[7]);

                long value = (bucketValue0 << 35)
                        + (bucketValue1 << 30)
                        + (bucketValue2 << 25)
                        + (bucketValue3 << 20)
                        + (bucketValue4 << 15)
                        + (bucketValue5 << 10)
                        + (bucketValue6 << 5)
                        + (bucketValue7);

                convertToBlock(value, plainDataBlock);

                int resultBlockSizeInBit = len * 5;
                int resultBlockSize = resultBlockSizeInBit / 8;

                byteArrayOutputStream.write(plainDataBlock, 0, resultBlockSize);

                Arrays.fill(encodedDataBlock, (byte) 0);
            }
        }

        private static long getTableValue(int[] tableDecode, byte value) {
            if (value <= 0) {
                return 0;
            }

            char key = (char) value;
            int tableValue = tableDecode[key];
            if (tableValue < 0) {
                throw new IllegalArgumentException(String.format("Invalid character %c detected.", key));
            }
            return tableValue;
        }

        private static void convertToBlock(long value, byte[] block) {
            block[0] = (byte) ((value & (0XFFL << 32)) >>> 32);
            block[1] = (byte) ((value & (0XFFL << 24)) >>> 24);
            block[2] = (byte) ((value & (0XFFL << 16)) >>> 16);
            block[3] = (byte) ((value & (0XFFL << 8))  >>> 8);
            block[4] = (byte) ((value &  0XFF));
        }
    }
}
