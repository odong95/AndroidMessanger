package com.waspteam.waspmessenger;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Julian on 11/16/2016.
 */



public class CryptoGenerator
{
    private File pkFile, skFile, pkNumFile, skNumFile;
    private String mUsername;
    private Context mContext;

    private CryptoGenerator()
    {
        //This will never execute
    }

    public CryptoGenerator(Context context, String name)
    {
        mUsername = name;
        mContext = context;

        //The number of keys we've made of each type (for writing new files)
        pkNumFile = new File(context.getFilesDir(), mUsername + "Num.pk");
        skNumFile = new File(context.getFilesDir(), mUsername + "Num.sk");

        //These are the key directory files
        pkFile = new File(context.getFilesDir(), mUsername + "Tag.pk");
        skFile = new File(context.getFilesDir(), mUsername + "Tag.sk");

        //Individual key files are aliased individually, as their byte number may change
    }

    private void writeKey(byte[] tag, byte[] key, boolean isPrivate)
    {
        int numKeys = 0;

        File keyFile = null;

        FileOutputStream fos = null;
        FileInputStream fis = null;

        try
        {
            //STAGE 1: FETCH NUMBER OF KEYS WE HAVE TO ALIAS THIS NEW KEY
            if (isPrivate)
            {
                if(pkNumFile.exists())
                {
                    fis = new FileInputStream(pkNumFile);
                    numKeys = fis.read();

                    fis.close();
                }

                //We are going to add a new key of this number
                numKeys++;

                fos = new FileOutputStream(pkNumFile);
            }
            else
            {
                if(skNumFile.exists())
                {
                    fis = new FileInputStream(skNumFile);
                    numKeys = fis.read();
                    fis.close();
                }

                //We are going to add a new key of this number
                numKeys++;

                fos = new FileOutputStream(skNumFile);
            }

            //Write the new number of keys to the type we are working on

            fos.write(numKeys);
            fos.close();

            //STAGE 2: WRITE THE HASHED TAG TO REGISTRY FILE
            if (isPrivate)
            {
                fos = new FileOutputStream(pkFile, true);
            }
            else
            {
                fos = new FileOutputStream(skFile, true);
            }

            fos.write(tag);
            fos.close();

            //STAGE 3: WRITE THE KEY INTO ITS OWN FILE
            //WE CAN FIND IT LATER BY ITERATING THROUGH THE REGISTRY FILE TO ALIAS IT
            if (isPrivate)
            {
                keyFile = new File(mContext.getFilesDir(), mUsername + "Key" + numKeys +".pk");
            }
            else
            {
                keyFile = new File(mContext.getFilesDir(), mUsername + "Key" + numKeys + ".sk");
            }

            fos = new FileOutputStream(keyFile);
            fos.write(key);
            fos.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private byte[] findKey(byte[] identifier, boolean isPrivate) throws RuntimeException
    {


        File keyFile = null;
        int read = 0;
        FileInputStream fis = null;
        byte[] readBytes = new byte[32];
        byte[] fetchKey = null;

        if(identifier.length!=32)
        {
            throw new RuntimeException();
        }



        try
        {

            if (isPrivate)
            {
                if (pkFile.exists())
                {
                    fis = new FileInputStream(pkFile);
                }
                else
                {
                    throw new RuntimeException();
                }
            }
            else
            {
                if (skFile.exists())
                {
                    fis = new FileInputStream(skFile);
                }
                else
                {
                    throw new RuntimeException();
                }
            }

            for(int i = 1 ; ((read = fis.read(readBytes))) != -1 ; i++)
            {
                if(java.util.Arrays.equals(identifier, readBytes))
                {
                    read=i;
                    break;
                }
            }

            fis.close();

            if(read==0)
            {
                throw new RuntimeException();
            }

            if (isPrivate)
            {

                keyFile = new File(mContext.getFilesDir(), mUsername + "Key" +read +".pk");
            }
            else
            {
                keyFile = new File(mContext.getFilesDir(), mUsername + "Key" +read +".sk");
            }

            if (!keyFile.exists())
            {
                throw new RuntimeException();
            }

            fis = new FileInputStream(keyFile);
            fetchKey = new byte[(int)keyFile.length()];
            fis.read(fetchKey);
            fis.close();

            return fetchKey;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }



        return null;
    }

    //Setup phase done by Person A in a conversation
    //Returns the public key to be sent to the server
    public String Phase1(String identifyingTag)
    {
        try
        {
            //Setup Diffie Hellman Parameters
            AlgorithmParameterGenerator apg = AlgorithmParameterGenerator.getInstance("DH");
            apg.init(1024);
            AlgorithmParameters params = apg.generateParameters();
            DHParameterSpec dhParamSpecA = params.getParameterSpec(DHParameterSpec.class);

            //Start generating key
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
            keyGen.initialize(dhParamSpecA);
            KeyPair keyPair = keyGen.generateKeyPair();

            //Extract private key to save for later reference
            byte[] storePriv = keyPair.getPrivate().getEncoded();

            //Generate tag bytes
            MessageDigest tagSha = MessageDigest.getInstance("SHA-256");
            byte[] tagBytes = tagSha.digest(identifyingTag.getBytes("ISO-8859-1"));

            //Save the key with the identifying tag
            this.writeKey(tagBytes, storePriv, true);

            //Send back the string encoded version of the string that will be stored to the server
            return new String(keyPair.getPublic().getEncoded(), "ISO-8859-1");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    //Final step for User B in a conversation
    //Returns public key to be sent to the server
    public String Phase2(String identifyingTag, String pubKeyAString)
    {

        try
        {
            //Recover encoded A key
            byte[] pubKeyAEnc = pubKeyAString.getBytes("ISO-8859-1");
            KeyFactory keyFac = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpecA = new X509EncodedKeySpec(pubKeyAEnc);
            PublicKey aPubKey = keyFac.generatePublic(x509KeySpecA);

            //Fetch the parameters originally used for generation
            DHParameterSpec dhParamSpecB = ((DHPublicKey)aPubKey).getParams();

            //Initialize B key generator
            KeyPairGenerator bKeyGen = KeyPairGenerator.getInstance("DH");
            bKeyGen.initialize(dhParamSpecB);

            //NOW GENERATE THE SHARED SECRET
            KeyPair bKeyPair = bKeyGen.generateKeyPair();

            //Create KeyAgreement to process public key
            KeyAgreement bAgree = KeyAgreement.getInstance("DH");
            bAgree.init(bKeyPair.getPrivate());
            bAgree.doPhase(aPubKey, true);

            //STORE THIS FOR LATER, ASSOCIATE WITH CONVERSATION
            byte[] bSecretKey = bAgree.generateSecret();
            MessageDigest bSha = MessageDigest.getInstance("SHA-256");
            byte[] digestBKey = bSha.digest(bSecretKey);
            SecretKey bAESsecret = new SecretKeySpec(digestBKey, 0, 16,"AES");
            byte[] finalAESkeyB = bAESsecret.getEncoded();

            //Generate tag bytes
            MessageDigest tagSha = MessageDigest.getInstance("SHA-256");
            byte[] tagBytes = tagSha.digest(identifyingTag.getBytes("ISO-8859-1"));

            //Save SECRET key with tag
            this.writeKey(tagBytes, finalAESkeyB, false);

            //PUBLIC KEY GOES TO SERVER
            return new String(bKeyPair.getPublic().getEncoded(), "ISO-8859-1");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    //Final step for User A in a conversation
    //Simply returns true if it manages to execute properly
    public boolean Phase3(String identifyingTag, String pubKeyBString)
    {
        try
        {
            //Decode pubKeyB
            byte[] pubKeyBEnc = pubKeyBString.getBytes("ISO-8859-1");
            KeyFactory keyFac = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpecB = new X509EncodedKeySpec(pubKeyBEnc);
            PublicKey bPubKey = keyFac.generatePublic(x509KeySpecB);

            //Get identifying tag bytes
            MessageDigest tagSha = MessageDigest.getInstance("SHA-256");
            byte[] tagBytes = tagSha.digest(identifyingTag.getBytes("ISO-8859-1"));


            //Get encoded private key A
            byte[] aStorePriv = findKey(tagBytes,true);


            //Recover private Key A
            KeyFactory aKeyFac = KeyFactory.getInstance("DH");
            //X509EncodedKeySpec x509KeySpecA = new X509EncodedKeySpec(bPubKeyEnc);
            PKCS8EncodedKeySpec PKCS8SecKeySpecA = new PKCS8EncodedKeySpec(aStorePriv);
            //PublicKey bPubKey = aKeyFac.generatePublic(x509KeySpecA);
            PrivateKey aPrivKey = aKeyFac.generatePrivate(PKCS8SecKeySpecA);

            //RELOAD aKeyPair from earlier!!!!
            KeyAgreement aAgree = KeyAgreement.getInstance("DH");
            aAgree.init(aPrivKey);
            aAgree.doPhase(bPubKey, true);

            //STORE THIS FOR LATER, ASSOCIATE WITH CONVERSATION
            //ALSO THROW OUT KEY PAIR FROM EARLIER!!!!
            byte[] aSecretKey = aAgree.generateSecret();
            MessageDigest aSha = MessageDigest.getInstance("SHA-256");
            byte[] digestAKey = aSha.digest(aSecretKey);
            SecretKey aAESsecret = new SecretKeySpec(digestAKey, 0, 16,"AES");
            byte[] finalAESkeyA = aAESsecret.getEncoded();

            //Save Secret Key A
            writeKey(tagBytes,finalAESkeyA,false);

            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    //For conversations already working, fetches the private key from storage
    public byte[] getSecretKey(String identifyingTag)
    {
        try
        {
            MessageDigest tagSha = MessageDigest.getInstance("SHA-256");
            byte[] tagBytes = tagSha.digest(identifyingTag.getBytes("ISO-8859-1"));
            return findKey(tagBytes,false);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

}
