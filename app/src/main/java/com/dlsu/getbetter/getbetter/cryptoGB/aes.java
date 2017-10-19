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

public class aes implements Serializable{

    private static final int AES_Key_Size = 256;
    private transient byte[] key;

    private transient SecretKeySpec secretkey;
    private transient Cipher cipher;

    public aes() throws Exception{
        this.setCipher();
        this.setKey();
    }

    public aes(SecretKeySpec key) throws Exception{
        this.secretkey = key;
        this.setCipher();
    }

    public void setKey() throws Exception{
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(AES_Key_Size);
        SecretKey aeskey = kgen.generateKey();
        key = aeskey.getEncoded();
        secretkey = new SecretKeySpec(key, "AES");
    }

    public void setCipher() throws Exception{
//        try {
            this.cipher = Cipher.getInstance("AES/CFB/PKCS5PADDING");
//        }catch(Exception e){
//            Log.e("error", e.toString());
//        }
    }

    public Cipher getCipher(){
        return this.cipher;
    }

    public SecretKeySpec getKey(){
        return this.secretkey;
    }

}
