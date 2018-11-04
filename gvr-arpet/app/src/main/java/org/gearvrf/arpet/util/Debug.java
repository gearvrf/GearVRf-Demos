package org.gearvrf.arpet.util;

import android.util.Log;

public class Debug {
    public static void log4x4Matrix(String tag, float[] matrix) {
        if (matrix.length != 16)
            return;

        Log.d(tag,"0: " + matrix[0] + " 4: " + matrix[4] + " 8: " + matrix[8] + " 12: " + matrix[12] +
                "\n1: " + matrix[1] + " 5: " + matrix[5] + " 9: " + matrix[9] + " 13: " + matrix[13] +
                "\n2: " + matrix[2] + " 6: " + matrix[6] + " 10: " + matrix[10] + " 14: " + matrix[14] +
                "\n3: " + matrix[3] + " 7: " + matrix[7] + " 11: " + matrix[11] + " 15: " + matrix[15]);
    }
}
