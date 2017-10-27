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
    private transient Cipher cipher;

    public aes(SecretKey key){
        this.AES_Key_Size = 256;
        this.secretkey = key;
        this.key = secretkey.getEncoded();
    }
//
    public aes(){
        this.AES_Key_Size = 256;
        this.setKey();
        this.setCipher();
    }

    public void setKey(){
        try{
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(AES_Key_Size);
            SecretKey aeskey = kgen.generateKey();
            key = aeskey.getEncoded();
            String temp = android.util.Base64.encodeToString(key, DEFAULT);
            secretkey = new SecretKeySpec(android.util.Base64.decode(temp, DEFAULT), "AES");
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
        out.defaultWriteObject();
//        out.writeInt(this.AES_Key_Size);
////        for(int i=0; i<this.key.length; i++){
////            Log.w("byte written", Byte.toString(this.key[i]));
////            out.writeByte(this.key[i]);
////        }
//        out.writeObject(key);
////        Log.w("key length", Integer.toString(this.key.length));
////        out.writeUTF(Integer.toString(this.key.length));
//
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        Log.w("serial?", "reading!");
        in.defaultReadObject();
        this.setCipher();
//        String temp = android.util.Base64.encodeToString(this.key, DEFAULT);
//        this.secretkey = new SecretKeySpec(android.util.Base64.decode(temp, DEFAULT), "AES");
//        byte[] stuff = new byte[in.available()];
//        in.readFully(stuff);
//        for(int i=0; i<stuff.length; i++){
//            Log.w("stuff", Byte.toString(stuff[i]));
//        }
////        in.defaultReadObject();
////        byte[] k = new byte[in.readInt()];
////        this.key = in.readObject();
////        byte[] b = new byte[in.available()];
//////        Log.w("reading", in.readFully(b));
//////        Log.w("reading", in.readUTF());
////        byte[] k = new byte[Integer.parseInt(in.readUTF())];
////        if(in.read(k, 0, k.length)>0){
////            Log.w("serial?", "got key!");
////            this.key = k;
////            setCipher();
////            this.secretkey = new SecretKeySpec(key, "AES");
////        } else Log.w("serial?", "no key...");
    }

}
