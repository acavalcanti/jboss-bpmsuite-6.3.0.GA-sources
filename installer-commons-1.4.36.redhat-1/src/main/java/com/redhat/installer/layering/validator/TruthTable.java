package com.redhat.installer.layering.validator;

import com.izforge.izpack.util.Debug;

/**
 * Represents a Truth Table.
 * Contains public methods for mapping logical inputs to outputs and vice versa.
 * Table values are represented by strings and characters.
 * ie.  ABCDE|Out
 *      00000|1
 *      00001|0
 *      ...
 *      11111|0
 *
 * A Table can be instantiated by invoking the constructor with a String containing the
 * desired output vector. See constructor doc for details.
 *
 *
 * Created by francisco canas on 1/23/14.
 * Source: https://github.com/FranciscoCanas/mintermer/blob/master/jtermer/src/logic/TruthTable.java
 */
public class TruthTable {
    String output;
    char [] outputVector;
    private Integer length;
    private Integer width;
    private Integer height;
    public static final char TRUE = '1';
    public static final char FALSE = '0';

    /**
     * Given a string containing the truth table's desired output vector, it generates a
     * truth table of the correct size.
     *
     * ie.: 01 ->   A | Out
     *              0   0
     *              1   1
     *
     *    : 0110 -> A   B   Out
     *              0   0   0
     *              0   1   1
     *              1   0   1
     *              1   1   0
     *
     *              ...and so forth.
     *
     * @param outputVector the String containing an output of at least >= 2 length.
     */
    public TruthTable(String outputVector) {
        if ((length = outputVector.length()) < 2) {
            Debug.log("Truth Table must have output length of at least 2");
            return;
        }

        output = outputVector;
        initDimensions();
        initOutputVector();
    }

    private void initDimensions() {
        double log = Math.log(length.doubleValue()) / Math.log(2);
        width = (int)Math.ceil(log);
        height = (int)Math.pow(2, width);

    }

    private void initOutputVector() {
        outputVector = new char[height];
        char [] outputChars = output.toCharArray();

        for (int i=0; i<height; i++) {
            if (i > output.length() - 1) {
                outputVector[i] = '0';
            } else {
                outputVector[i] = outputChars[i];
            }
        }
    }

    private String makeRow(int rowNum) {
        StringBuilder sb = new StringBuilder();
        String vec = Integer.toBinaryString(rowNum);
        int padLength = width - vec.length();

        for (int i=0; i<padLength; i++){
            sb.append("0");
        }

        sb.append(vec);
        return sb.toString();
    }

    public String getRow(int num) {
        if (num < height && num < outputVector.length) {
            return makeRow(num);
        } else {
            return "";
        }
    }

    public boolean getOutput(String input) {
        int num = Integer.parseInt(input, 2);
        if (num < height && num < outputVector.length) {
            return outputVector[num]==TRUE;
        } else {
            return false;
        }
    }

    public Integer getLength() {
        return length;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();


        for (int i=0; i< height; i++) {
            sb.append(makeRow(i) +  "|" + outputVector[i] + System.getProperty("line.separator"));
        }
        return sb.toString();
    }

}