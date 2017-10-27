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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
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

import static android.util.Base64.DEFAULT;

public class aes implements Serializable {

    private final int AES_Key_Size;
    private byte[] key;

    private SecretKey secretkey;
    private Cipher cipher;

    public aes(SecretKeySpec key){
        this.AES_Key_Size = 256;
        this.secretkey = key;
        this.key = secretkey.getEncoded();
    }
//
    public aes(){
        this.AES_Key_Size = 256;
    }

    public void setKey(){
        try{
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(AES_Key_Size);
            SecretKey aeskey = kgen.generateKey();
            key = aeskey.getEncoded();
            String temp = android.util.Base64.encodeToString(key, DEFAULT);
            key = android.util.Base64.decode(temp, DEFAULT);
            secretkey = new SecretKeySpec(key, "AES");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setCipher(){
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        }catch(Exception e){
            Log.e("error", e.toString());
        }
    }

    public Cipher getCipher(){
        return this.cipher;
    }

    public SecretKey getKey(){
        return this.secretkey;
    }

//    public void saveKey(String fileloc) throws Exception {
//        //save key to file
//        System.out.println("Trying to save key in " + fileloc);
//        OutputStream output = null;
//        try {
//            System.out.println("Saving key in file");
//            output = new BufferedOutputStream(new FileOutputStream(fileloc));
//            output.write(this.secretkey.getEncoded());
//        } finally{
//            output.close();
//            System.out.println("Successfully saved key");
//        }
//
//    }
//
//    public void retrieveKey(String fileloc) throws IOException{
//        //get key from file
//        System.out.println("Trying to get key from " + fileloc);
//        byte[] result = new byte[(int)new File(fileloc).length()];
//        try{
//         InputStream input = new BufferedInputStream(new FileInputStream(fileloc));
//         input.read(result);
//        } finally{
//            this.secretkey = new SecretKeySpec(result, 0, result.length, "AES");
//            System.out.println("Key successfully retrieved!");
//        }
//
//    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        Log.w("serial?", "writing!");
//        String temp = android.util.Base64.encodeToString(key, DEFAULT);
//        key = android.util.Base64.decode(temp, DEFAULT);
        out.writeUTF(Integer.toString(this.key.length));
        String t = android.util.Base64.encodeToString(this.key, DEFAULT);
        this.key = android.util.Base64.decode(t, DEFAULT);
        out.write(this.key);
//        for(int i=0; i<this.key.length; i++){
//            out.writeUTF(Byte.toString(this.key[i]));
//            out.write()
//        }
    }

    private void readObject(ObjectInputStream in) throws IOException{
        Log.w("serial?", "reading!");
        byte[] k = new byte[Integer.parseInt(in.readUTF())];
        try{
            in.readFully(k);
            this.key = k;
            setCipher();
            this.secretkey = new SecretKeySpec(this.key, "AES");
        } catch(Exception e){
            Log.w("error", e.getMessage());
        }
//        if(in.read(k, 0, k.length)>0){
//            Log.w("serial?", "got key!");
//            this.key = k;
//            setCipher();
//            this.secretkey = new SecretKeySpec(key, "AES");
//        } else Log.w("serial?", "no key...");
    }

}
