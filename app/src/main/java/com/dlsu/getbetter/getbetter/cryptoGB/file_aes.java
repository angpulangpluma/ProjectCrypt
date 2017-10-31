package com.dlsu.getbetter.getbetter.cryptoGB;

/**
 * Created by YING LOPEZ on 9/28/2017.
 */

import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class file_aes {

    private aes filealgo;
//    private Cipher ciph;

    public file_aes(){
        filealgo = new aes();
        filealgo.setKey();
        filealgo.setCipher();
//        ciph = filealgo.getCipher();
    }

    public file_aes(aes enc){
        this.filealgo = enc;
        filealgo.setCipher();
//        this.ciph = filealgo.getCipher();
    }

    public aes getCrypt(){
        return filealgo;
    }

    public void encryptFile(File file){
        Log.w("encrypt?", "start");
//        File encrypted = new File(file.getPath() + "_encrypted."+ FilenameUtils.getExtension(file.getName()));
        Log.w("encrypt file", file.getPath());
        Cipher cp = filealgo.getCipher();
        SecretKey k = filealgo.getKey();
        try{
            FileInputStream in = new FileInputStream(file);
            cp.init(Cipher.ENCRYPT_MODE, k);
            Log.w("file length", Long.toString(file.length()));
            byte[] buffer = new byte[(int)file.length()];
            Log.w("buffer length", Integer.toString(buffer.length));
            byte[] encfile = null;
            if (in.read(buffer)!=-1){
                in.close();
                encfile = cp.doFinal(buffer);
                FileOutputStream os = new FileOutputStream(file);
                os.write(encfile);
                Log.w("encrypt file", "done");
                os.close();
            } else Log.w("encrypt file", "failed");
        } catch(Exception ex){
            Log.w("error", ex.getMessage());
        }
    }

    public void decryptFile(File file){
//        File decrypted = new File(file.getPath() + "//" + returnFileName(file)+"_decrypted."+returnFileExt(file));
        Cipher cp = filealgo.getCipher();
        SecretKey k = filealgo.getKey();
        Log.w("decrypt?", "start");
        Log.w("file", file.toString());
        try{
            FileInputStream in = new FileInputStream(file);
            cp.init(Cipher.DECRYPT_MODE, k);
            Log.w("file length", Long.toString(file.length()));
            byte[] buffer = new byte[(int)file.length()];
            Log.w("buffer length", Integer.toString(buffer.length));
            byte[] encfile = null;
            if (in.read(buffer)!=-1){
                in.close();
                encfile = cp.doFinal(buffer);
                FileOutputStream os = new FileOutputStream(file);
                os.write(encfile);
                Log.w("decrypt file", "done");
                os.close();
            } else Log.w("decrypt file", "failed");
        } catch(Exception ex){
            Log.w("error", ex.getMessage());
        }
    }

    private String returnFileExt(File file){
        String ext = "";
        int i = file.getName().lastIndexOf('.');
        if (i >= 0)
            ext = file.getName().substring(i+1);
        return ext;
    }

    private String returnFileName(File file){
        String filename = "";
        int i = file.getName().lastIndexOf('.');
        if (i >= 0)
            filename = file.getName().substring(0, i);
        return filename;
    }

    /*
    Implementation for copy() from www.macs.hw.ac.uk/~ml355/lore/FileEncryption.java
    */
    private void copy(InputStream is, OutputStream os){
        int i;
        byte[] b = new byte[8];
        try{
            while((i=is.read(b))!=-1) {
                os.write(b, 0, i);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
