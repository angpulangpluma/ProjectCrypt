package com.dlsu.getbetter.getbetter.cryptoGB;

/**
 * Created by YING LOPEZ on 9/28/2017.
 */

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class aes {

    private static final int AES_Key_Size = 256;
    private byte[] key;

    private SecretKeySpec secretkey;
    private Cipher cipher;

//    public aes(SecretKeySpec key){
//        this.secretkey = key;
//    }
//
//    public aes(){
//
//    }

    public void setKey(){
        try{
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(AES_Key_Size);
            SecretKey aeskey = kgen.generateKey();
            key = aeskey.getEncoded();
            secretkey = new SecretKeySpec(key, "AES");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setCipher(){
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        }catch(Exception e){
            Log.e("error", e.toString());
        }
    }

    public Cipher getCipher(){
        return this.cipher;
    }

    public SecretKeySpec getKey(){
        return this.secretkey;
    }

    public void saveKey(String fileloc) throws Exception {
        //save key to file
        System.out.println("Trying to save key in " + fileloc);
        OutputStream output = null;
        try {
            System.out.println("Saving key in file");
            output = new BufferedOutputStream(new FileOutputStream(fileloc));
            output.write(this.secretkey.getEncoded());
        } finally{
            output.close();
            System.out.println("Successfully saved key");
        }

    }

    public void retrieveKey(String fileloc) throws IOException{
        //get key from file
        System.out.println("Trying to get key from " + fileloc);
        byte[] result = new byte[(int)new File(fileloc).length()];
        try{
         InputStream input = new BufferedInputStream(new FileInputStream(fileloc));
         input.read(result);
        } finally{
            this.secretkey = new SecretKeySpec(result, 0, result.length, "AES");
            System.out.println("Key successfully retrieved!");
        }

    }

}
