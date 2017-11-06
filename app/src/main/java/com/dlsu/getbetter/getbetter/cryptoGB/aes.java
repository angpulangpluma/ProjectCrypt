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
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import static android.util.Base64.DEFAULT;
import static android.util.Base64.decode;
import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.codec.binary.Hex.encodeHex;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.io.FileUtils.writeStringToFile;

public class aes implements Serializable {

    private final int AES_Key_Size;
    private byte[] key;
    private byte[] iv;

    private SecretKey secretkey;
    private Cipher cipher;
    private static final String ALGO = "AES";
    private IvParameterSpec ivParamSpec;

    public aes(SecretKey key, IvParameterSpec ivParamSpec){
        this.AES_Key_Size = 256;
        this.secretkey = key;
        this.key = secretkey.getEncoded();
        this.ivParamSpec = ivParamSpec;
        this.iv = ivParamSpec.getIV();
    }
    public aes(SecretKey key){
        this.AES_Key_Size = 256;
        this.secretkey = key;
        this.key = secretkey.getEncoded();
        this.setIV();
    }
    //
    public aes(){
        this.AES_Key_Size = 256;
        this.setKey();
        this.setCipher();
        this.setIV();
    }

    public void setKey(){
        try{
            KeyGenerator kgen = KeyGenerator.getInstance(ALGO);
            kgen.init(AES_Key_Size);
            SecretKey aeskey = kgen.generateKey();
            secretkey = new SecretKeySpec(aeskey.getEncoded(), ALGO);
            key = secretkey.getEncoded();
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

    public void setIV(){
        iv = new byte[cipher.getBlockSize()];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        ivParamSpec = new IvParameterSpec(iv);

    }

    public void resetIV(){
        ivParamSpec = new IvParameterSpec(iv);
    }

    public Cipher getCipher(){
        return this.cipher;
    }

    public SecretKey getKey(){
        return this.secretkey;
    }

    public IvParameterSpec getIvParamSpec(){ return this.ivParamSpec; }

    /*
    * @author stuinzuri
    * @source https://github.com/stuinzuri/SimpleJavaKeyStore/blob/master/src/ch/geekomatic/sjks/KeyStoreUtils.java
    */
    public void saveKey(SecretKey key, File file) throws IOException
    {
        Log.w("savekey?", "yes!");
        byte[] encoded = key.getEncoded();
        char[] hex = encodeHex(encoded);
        char[] ch = new char[encoded.length];
        String data = String.valueOf(hex);
        String param = "";
        if (!data.isEmpty()){
            Log.w("key hexed", data);
            for(int i=0; i<ch.length; i++)
                ch[i] = Byte.valueOf(encoded[i]).toString().charAt(0);
            Log.w("key orig", String.valueOf(ch));
            hex = encodeHex(iv);
            param = String.valueOf(hex);
        } else Log.w("key", "failed");
        writeStringToFile(file, data + " " + param);
    }

    /*
    * @author stuinzuri
    * @source https://github.com/stuinzuri/SimpleJavaKeyStore/blob/master/src/ch/geekomatic/sjks/KeyStoreUtils.java
    */
    public void loadKey(File file) throws IOException
    {
        Log.w("loadkey?", "yes");
        String[] data = new String(readFileToByteArray(file)).split(" ");
        char[] hex = data[0].toCharArray();
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
            Log.w("file data", data[0]);
            char[] ch = new char[encoded.length];
            for(int i=0; i<ch.length; i++)
                ch[i] = Byte.valueOf(encoded[i]).toString().charAt(0);
            Log.w("key", String.valueOf(ch));
            secretkey = new SecretKeySpec(encoded, ALGO);
            key = secretkey.getEncoded();
            hex = data[1].toCharArray();
            try{
                encoded = decodeHex(hex);
            } catch(DecoderException e){
                Log.w("error", e.getMessage());
            }
            Log.w("iv data", data[1]);
            ivParamSpec = new IvParameterSpec(encoded);
            iv = encoded;
        }
    }

    /*
    * @author stuinzuri
    * @source https://github.com/stuinzuri/SimpleJavaKeyStore/blob/master/src/ch/geekomatic/sjks/KeyStoreUtils.java
    */
    public void loadKeyFinal(File file) throws IOException
    {
        Log.w("loadkey?", "yes");
        String[] data = new String(readFileToByteArray(file)).split(" ");
        char[] hex = data[0].toCharArray();
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
            Log.w("file data", data[0]);
            char[] ch = new char[encoded.length];
            for(int i=0; i<ch.length; i++)
                ch[i] = Byte.valueOf(encoded[i]).toString().charAt(0);
            Log.w("key", String.valueOf(ch));
            secretkey = new SecretKeySpec(encoded, ALGO);
            key = secretkey.getEncoded();
            hex = data[1].toCharArray();
            try{
                encoded = decodeHex(hex);
            } catch(DecoderException e){
                Log.w("error", e.getMessage());
            }
            Log.w("file data", data[1]);
            ch = new char[encoded.length];
            for(int i=0; i<ch.length; i++)
                ch[i] = Byte.valueOf(encoded[i]).toString().charAt(0);
            Log.w("iv", String.valueOf(ch));
            iv = encoded;
            ivParamSpec = new IvParameterSpec(iv);
        }
    }

}
