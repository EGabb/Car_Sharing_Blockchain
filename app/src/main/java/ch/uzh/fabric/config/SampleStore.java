/*
 *  Copyright 2016, 2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 	  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ch.uzh.fabric.config;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A local file-based key value store.
 */
public class SampleStore extends JsonObject {

  private String file;
  private final Log LOGGER = LogFactory.getLog(SampleStore.class);
  private Map<String, SampleUser> members = new HashMap<>();


  public SampleStore(File file) {

    this.file = file.getAbsolutePath();
  }

  /**
   * Get the value associated with name.
   *
   * @param name
   * @return value associated with the name
   */
  public String getValue(String name) {
    Properties properties = loadProperties();
    return properties.getProperty(name);
  }

  private Properties loadProperties() {
    Properties properties = new Properties();
    try (InputStream input = new FileInputStream(file)) {
      properties.load(input);
      input.close();
    } catch (FileNotFoundException e) {
      LOGGER.warn(String.format("Could not find the file \"%s\"", file));
    } catch (IOException e) {
      LOGGER.warn(String.format("Could not load keyvalue store from file \"%s\", reason:%s",
        file, e.getMessage()));
    }

    return properties;
  }

  /**
   * Set the value associated with name.
   *
   * @param name The name of the parameter
   * @param value Value for the parameter
   */
  public void setValue(String name, String value) {
    Properties properties = loadProperties();
    try (
      OutputStream output = new FileOutputStream(file)) {
      properties.setProperty(name, value);
      properties.store(output, "");
      output.close();

    } catch (IOException e) {
      LOGGER.warn(String.format("Could not save the keyvalue store, reason:%s", e.getMessage()));
    }
  }

  /**
   * Get the user with a given name
   *
   * @param name
   * @param org
   * @return user
   */
  public SampleUser getMember(String name, String org) {

    // Try to get the SampleUser state from the cache
    SampleUser sampleUser = getMembers().get(SampleUser.toKeyValStoreName(name, org));
    if (null != sampleUser) {
      return sampleUser;
    }

    // Create the SampleUser and try to restore it's state from the key value store (if found).
    sampleUser = new SampleUser(name, org, this);

    return sampleUser;

  }

  /**
   * Get the user with a given name
   *
   * @param name
   * @param org
   * @param sampleUser
   */
  public void putMember(String name, String org, SampleUser sampleUser) {

    // Try to get the SampleUser state from the cache
    getMembers().put(SampleUser.toKeyValStoreName(name, org), sampleUser);
  }

  /**
   * Get the user with a given name
   *
   * @return user
   */
  public SampleUser getMember(String name, String org, String MSPID, File privateKeyFile,
    File certificateFile) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {

    try {
      // Try to get the SampleUser state from the cache
      SampleUser sampleUser = getMembers().get(SampleUser.toKeyValStoreName(name, org));
      if (null != sampleUser) {
        return sampleUser;
      }

      // Create the SampleUser and try to restore it's state from the key value store (if found).
      sampleUser = new SampleUser(name, org, this);
      sampleUser.setMPSID(MSPID);

      String certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), "UTF-8");

      //PrivateKey privateKey = getPrivateKeyFromFile(privateKeyFile);
      PrivateKey privateKey = getPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream(privateKeyFile)));

      sampleUser.setEnrollment(new SampleStoreEnrollement(privateKey, certificate));

      sampleUser.saveState();

      return sampleUser;
    } catch (IOException e) {
      e.printStackTrace();
      throw e;

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw e;
    } catch (NoSuchProviderException e) {
      e.printStackTrace();
      throw e;
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
      throw e;
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw e;
    }

  }

  static {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
  }

  static PrivateKey getPrivateKeyFromBytes(byte[] data) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {

    final PEMParser pemParser = new PEMParser(new StringReader(new String(data)));

    PrivateKeyInfo pemPair = (PrivateKeyInfo) pemParser.readObject();

    PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(pemPair);

    return privateKey;
  }

  /**
   * @return the members
   */
  public Map<String, SampleUser> getMembers() {
    return members;
  }

  /**
   * @param members the members to set
   */
  public void setMembers(Map<String, SampleUser> members) {
    this.members = members;
  }

  static class SampleStoreEnrollement implements Enrollment, Serializable {

    private PrivateKey privateKey;
    // private transient PrivateKey privateKey;
    private final String certificate;

    SampleStoreEnrollement(PrivateKey privateKey, String certificate) {

      this.certificate = certificate;

      this.privateKey = privateKey;
    }

    @Override
    public PrivateKey getKey() {

      return privateKey;
    }

    @Override
    public String getCert() {
      return certificate;
    }

  }

}
