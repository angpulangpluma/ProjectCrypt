package com.dlsu.getbetter.getbetter.cryptoGB;

import android.os.Environment;

import com.dlsu.getbetter.getbetter.DirectoryConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

/**
 * Created by student on 10/11/2017.
 */

public class KeySetter {

    public static void main(String[] args){
        aes master = new aes();
        File out = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS),
                "tpyrc");
        try {
            OutputStream on = new FileOutputStream(out);
            // TODO: know how to get number of health centers
            // TODO: for each health center, generate cryptography key
        } catch(Exception e){
            e.printStackTrace();
        }
        //get number of health centers
        //for each health center
    }

}
