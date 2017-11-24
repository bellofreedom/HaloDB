package amannaly;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Arjun Mannaly
 */
public class Record {

    //TODO: move to Header.
    public static final int KEY_SIZE_OFFSET = 0;
    public static final int VALUE_SIZE_OFFSET = 2;
    public static final int FLAGS_OFFSET = 6;

    /**
     * key size     - 2 bytes.
     * value size   - 4 bytes.
     * flags         - 1 byte.
     */
    public static final int HEADER_SIZE = 7;

    private final byte[] key, value;

    public static final byte[] TOMBSTONE_VALUE = new byte[0];

    private RecordMetaDataForCache recordMetaData;

    private Header header;

    public Record(byte[] key, byte[] value) {
        this.key = key;
        this.value = value;

        header = new Header((short)key.length, value.length, (byte)0);
    }

    public ByteBuffer[] serialize() {
        return new ByteBuffer[] {header.serialize(), ByteBuffer.wrap(key), ByteBuffer.wrap(value)};
    }

    public static Record deserialize(ByteBuffer buffer, short keySize, int valueSize) {
        buffer.flip();

        byte[] key = new byte[keySize];
        byte[] value = new byte[valueSize];

        buffer.get(key);
        buffer.get(value);

        return new Record(key, value);
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public RecordMetaDataForCache getRecordMetaData() {
        return recordMetaData;
    }

    public void setRecordMetaData(RecordMetaDataForCache recordMetaData) {
        this.recordMetaData = recordMetaData;
    }

    public int getRecordSize() {
        return header.getRecordSize();
    }

    public byte getFlags() {
        return header.getFlags();
    }

    // tombstones will have the lsb of flags set to 1.
    public void markAsTombStone() {
        header.markAsTombStone();
    }

    public boolean isTombStone() {
        return header.isTombStone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Record)) {
            return false;
        }

        Record record = (Record)obj;

        if (getKey() == null || record.getKey() == null)
            return false;

        if (getValue() == null || record.getValue() == null)
            return false;

        return Arrays.equals(getKey(), record.getKey()) && Arrays.equals(getValue(), record.getValue());
    }

    //TODO: override hash code.


    public static class Header {
        private short keySize;
        private int valueSize;
        private byte flags;

        private int recordSize;

        public Header(short keySize, int valueSize, byte flags) {
            this.keySize = keySize;
            this.valueSize = valueSize;
            this.flags = flags;

            recordSize = keySize + valueSize + HEADER_SIZE;
        }

        public static Header deserialize(ByteBuffer buffer) {

            short keySize = buffer.getShort(KEY_SIZE_OFFSET);
            int valueSize = buffer.getInt(VALUE_SIZE_OFFSET);
            byte flags = buffer.get(FLAGS_OFFSET);

            return new Header(keySize, valueSize, flags);
        }

        public ByteBuffer serialize() {
            byte[] header = new byte[HEADER_SIZE];

            ByteBuffer headerBuffer = ByteBuffer.wrap(header);
            headerBuffer.putShort(KEY_SIZE_OFFSET, keySize);
            headerBuffer.putInt(VALUE_SIZE_OFFSET, valueSize);
            headerBuffer.put(FLAGS_OFFSET, flags);

            return headerBuffer;
        }

        // tombstones will have the lsb of flags set to 1.
        public void markAsTombStone() {
            flags = (byte)(flags | 1);
        }

        /**
         * tombstones will have the lsb of flags set to 1
         * and no value.
         */
        public boolean isTombStone() {
            return (flags & 1) == 1 && valueSize == 0;
        }

        public short getKeySize() {
            return keySize;
        }

        public int getValueSize() {
            return valueSize;
        }

        public byte getFlags() {
            return flags;
        }

        public int getRecordSize() {
            return recordSize;
        }
    }
}
