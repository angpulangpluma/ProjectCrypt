package com.dlsu.getbetter.getbetter.cryptoGB;

/**
 * Created by YING LOPEZ on 9/28/2017.
 */

import org.apache.commons.codec.binary.Base64;

import java.io.Serializable;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class text_aes{

    private transient aes textalgo;
//    private Cipher ciph;

    public text_aes() throws Exception{
        textalgo = new aes();
        textalgo.setKey();
        textalgo.setCipher();
    }

    public text_aes(aes enc) throws Exception{
        this.textalgo = enc;
        textalgo.setCipher();
//        this.ciph = textalgo.getCipher();
    }

    public aes getCrypt(){
        return textalgo;
    }

    public String getEncString(String str){
        String decrypted = "";
        Cipher ciph = textalgo.getCipher();
        SecretKeySpec key = textalgo.getKey();
        try{
            ciph.init(Cipher.ENCRYPT_MODE, key);
            decrypted = new String(ciph.doFinal(Base64.decodeBase64(str)));
        }catch(Exception e){
            e.printStackTrace();
        }
        return decrypted;
    }

    public String getDecString(String str){
        String encrypted = "";
        Cipher ciph = textalgo.getCipher();
        SecretKeySpec key = textalgo.getKey();
        try{
            ciph.init(Cipher.DECRYPT_MODE, key);
            encrypted = Base64.encodeBase64String(ciph.doFinal(str.getBytes("UTF-8")));
        }catch(Exception e){
            e.printStackTrace();
        }
        return encrypted;
       }

    /*
    //use SHA-256 with salt start and end, check whether adding salt per
    //letter works for both web and mobile based on how fast it goes
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
    */
    public String getHashedString(String word){
        String result = "";
        byte[] hashed;
        MessageDigest digest;
        try{
            digest = MessageDigest.getInstance("SHA-256");
            hashed = digest.digest(word.getBytes("UTF-8"));
            result = new String(hashed, "UTF-8");
        } catch(Exception e){
            e.printStackTrace();
            result = "";
        }
        return result;
    }

}
