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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

import static android.util.Base64.DEFAULT;
import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.codec.binary.Hex.encodeHex;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.io.FileUtils.writeStringToFile;

public class aes implements Serializable {

    private final int AES_Key_Size;
    private byte[] key;

    private SecretKey secretkey;
    private Cipher cipher;
    private static final String ALGO = "AES";

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

    /*
    * @author stuinzuri
    * @source https://github.com/stuinzuri/SimpleJavaKeyStore/blob/master/src/ch/geekomatic/sjks/KeyStoreUtils.java
    */
    public void saveKey(SecretKey key, File file) throws IOException
    {
        byte[] encoded = key.getEncoded();
        char[] hex = encodeHex(encoded);
        String data = String.valueOf(hex);
        writeStringToFile(file, data);
    }

    /*
    * @author stuinzuri
    * @source https://github.com/stuinzuri/SimpleJavaKeyStore/blob/master/src/ch/geekomatic/sjks/KeyStoreUtils.java
    */
    public void loadKey(File file) throws IOException
    {
        String data = new String(readFileToByteArray(file));
        char[] hex = data.toCharArray();
        byte[] encoded = null;
        try
        {
            encoded = decodeHex(hex);
        }
        catch (DecoderException e)
        {
            Log.w("error", e.getMessage());
//            return null;
        }
        if (encoded!=null) {
            secretkey = new SecretKeySpec(encoded, ALGO);
            key = secretkey.getEncoded();
        }
//            SecretKey key = new SecretKeySpec(encoded, ALGO);
//        return key;
    }

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
