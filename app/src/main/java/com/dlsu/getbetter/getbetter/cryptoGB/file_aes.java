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
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class file_aes {

    private aes filealgo;

    public file_aes(){
        filealgo = new aes();
        filealgo.setKey();
        filealgo.setCipher();
//        filealgo.setIV();
    }

    public file_aes(aes enc){
        this.filealgo = enc;
        filealgo.setCipher();
    }

    public aes getCrypt(){
        return filealgo;
    }

    public void encryptFile(File file){
        Log.w("encrypt?", "start");
//        File encrypted = new File(file.getPath() + "_encrypted."+ FilenameUtils.getExtension(file.getName()));
//        if (filealgo.getIvParamSpec()==null)
//            filealgo.setIV();
        Log.w("encrypt file", file.getPath());
        Cipher cp = filealgo.getCipher();
        SecretKey k = filealgo.getKey();
        try{
            FileInputStream in = new FileInputStream(file);
            cp.init(Cipher.ENCRYPT_MODE, k, filealgo.getIvParamSpec());
            Log.w("file length", Long.toString(file.length()));
            byte[] buffer = new byte[(int)file.length()];
            Log.w("buffer length", Integer.toString(buffer.length));
            byte[] encfile = null;
            if (in.read(buffer)!=-1){
//                char[] data = new char[buffer.length];
//                for(int i=0; i<data.length; i++) {
//                    data[i] = Byte.valueOf(buffer[i]).toString().charAt(0);
//                }
//                Log.w("data", String.valueOf(data));
                in.close();
                Log.w("file length", Long.toString(file.length()));
                encfile = cp.doFinal(buffer);
//                data = new char[encfile.length];
//                for(int i=0; i<data.length; i++) {
//                    data[i] = Byte.valueOf(encfile[i]).toString().charAt(0);
//                }
//                Log.w("data", String.valueOf(data));
                FileOutputStream os = new FileOutputStream(file);
                os.write(encfile);
                Log.w("encrypt file", "done");
                Log.w("file length", Long.toString(file.length()));
                os.close();
                filealgo.resetIV();
            } else Log.w("encrypt file", "failed");
        } catch(GeneralSecurityException | IOException ex){
            Log.w("error", ex.getMessage());
        }
    }

    public byte[] decryptFile(File file){
        Log.w("decrypt?", "start");
//        File decrypted = new File(file.getPath() + "//" + returnFileName(file)+"_decrypted."+returnFileExt(file));
        Cipher cp = filealgo.getCipher();
        SecretKey k = filealgo.getKey();
        Log.w("file length", Long.toString(file.length()));
        Log.w("decrypt file", file.getPath());
//        byte[] output = null;
        byte[] encfile = null;
        try{
            FileInputStream in = new FileInputStream(file);
            cp.init(Cipher.DECRYPT_MODE, k, filealgo.getIvParamSpec());
            Log.w("file length", Long.toString(file.length()));
            byte[] buffer = new byte[(int)file.length()];
            Log.w("buffer length", Integer.toString(buffer.length));
            if (in.read(buffer)!=-1){
//                char[] data = new char[buffer.length];
//                for(int i=0; i<data.length; i++) {
//                    data[i] = Byte.valueOf(buffer[i]).toString().charAt(0);
//                }
//                Log.w("data", String.valueOf(data));
                in.close();
                Log.w("file length", Long.toString(file.length()));
                encfile = cp.doFinal(buffer);
                //
//                data = new char[encfile.length];
//                for(int i=0; i<data.length; i++) {
//                    data[i] = Byte.valueOf(encfile[i]).toString().charAt(0);
//                }
//                Log.w("data", String.valueOf(data));
//                Log.w("decfile length", Integer.toString(encfile.length));
//                FileOutputStream os = new FileOutputStream(file);
//                os.write(encfile);
//                Log.w("decrypt file", "done");
//                os.close();
//                Log.w("file length", Long.toString(file.length()));
                filealgo.resetIV();
            } else Log.w("decrypt file", "failed");
        } catch(GeneralSecurityException | IOException ex){
            Log.w("error", ex.getMessage());
        }

        return encfile;
    }

}
