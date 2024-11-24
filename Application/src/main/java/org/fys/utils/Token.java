
package org.fys.utils;

import java.util.ArrayList;
import java.util.List;

public class Token {

    public static final int MODEL_BRK = 1;
    public static final int VIEW_BRK = 2;
    public static final int MODEL_ON =3;
    public static final int VIEW_ON = 4;

    
    public static final int IMAGE_LOADED = 5;
    public static final int MODEL_LOADED = 6;
    public static final int INITIALIZE = 7;
    public static final int IMAGE_PROCESSED = 8;
    public static final int TEST = 9;

    public static final int S_OK = 10;
    public static final int S_NO = 11;
    public static final int MEMORY_FULL = 12; 

    public static final int CTR_ID = 13;
    public static final int VP_ID = 14;
    public static final int MODEL_ID = 15;

    private final String user_input_text;
    private final int msg;


    public Token(String user_input_text, int msg) {
        this.user_input_text= user_input_text;
        this.msg = msg;
    }

    public String user_input() {
        return user_input_text;
    }

    public  int read() {
        return msg;
    }
}
