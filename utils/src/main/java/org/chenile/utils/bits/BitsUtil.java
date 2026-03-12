package org.chenile.utils.bits;

public class BitsUtil {
    public static boolean isScopeEnabled(byte[] scopeArray, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitPosition = bitIndex % 8;

        // Bounds check
        if (byteIndex >= scopeArray.length) return false;

        // Use a mask to isolate the bit
        int mask = 1 << bitPosition;
        return (scopeArray[byteIndex] & mask) != 0;
    }
    public static void setScope(byte[] array, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitPosition = bitIndex % 8; // 0 is rightmost bit, 7 is leftmost

        // Create a mask (e.g., if bitPosition is 1, mask is 00000010)
        int mask = 1 << bitPosition;
        if (byteIndex > array.length){
            throw new RuntimeException("Attempt to set the bit " + bitIndex + " outside the range of the byte array " +
                    array.length);
        }

        // Apply OR operation and cast back to byte
        array[byteIndex] |= (byte) mask;
    }

    public static void main(String[] args){
        byte[] bytes = new byte[100] ;
        setScope(bytes,8);
        setScope(bytes,85);
        setScope(bytes,102);
        setScope(bytes,299);
        System.out.println("checking if 8th byte is set: " + isScopeEnabled(bytes,8));
        System.out.println("checking if 9th byte is set: " + isScopeEnabled(bytes,9));
        System.out.println("Checking if 85th byte is set: " + isScopeEnabled(bytes,85));
        System.out.println("Checking if 102 byte is set: " + isScopeEnabled(bytes,102));
        System.out.println("Checking if 299 byte is set: " + isScopeEnabled(bytes,299));
    }
}
