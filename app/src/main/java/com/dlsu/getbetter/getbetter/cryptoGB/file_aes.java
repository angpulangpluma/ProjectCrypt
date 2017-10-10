package com.dlsu.getbetter.getbetter.cryptoGB;

/**
 * Created by YING LOPEZ on 9/28/2017.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
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
//        this.ciph = filealgo.getCipher();
    }

    public aes getCrypt(){
        return filealgo;
    }

    public void encryptFile(File file){
        File encrypted = new File(file.getPath() + "_encrypted."+returnFileExt(file));
        Cipher cp = filealgo.getCipher();
        SecretKeySpec k = filealgo.getKey();
        try{
            FileInputStream in = new FileInputStream(file);
            cp.init(Cipher.ENCRYPT_MODE, k);
            CipherOutputStream os = new CipherOutputStream(new FileOutputStream(encrypted),
                    cp);
            copy(in, os);
            in.close();
            os.close();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void decryptFile(File file){
        File decrypted = new File(file.getPath() + "//" + returnFileName(file)+"_decrypted."+returnFileExt(file));
        Cipher cp = filealgo.getCipher();
        SecretKeySpec k = filealgo.getKey();
        try{
            FileOutputStream os = new FileOutputStream(decrypted);
            cp.init(Cipher.DECRYPT_MODE, k);
            CipherInputStream is = new CipherInputStream(new FileInputStream(file),
                    cp);
            copy(is, os);
            is.close();
            os.close();
        } catch(Exception ex){
            ex.printStackTrace();
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
        byte[] b = new byte[1024];
        try{
            while((i=is.read(b))!=-1) {
                os.write(b, 0, i);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
