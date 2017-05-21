package com.noryar.security.util.license;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlichtherle.license.CipherParam;
import de.schlichtherle.license.DefaultCipherParam;
import de.schlichtherle.license.DefaultKeyStoreParam;
import de.schlichtherle.license.DefaultLicenseParam;
import de.schlichtherle.license.KeyStoreParam;
import de.schlichtherle.license.LicenseContentException;
import de.schlichtherle.license.LicenseManager;
import de.schlichtherle.license.LicenseParam;

/**
 * 功能描述：license验证者.
 * @author Leon.
 *
 */
public class LicenseVerifier {
	private Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * 公钥别名
	 */
	private String pubAlias;
	/**
	 * 该密码是在使用keytool生成密钥对时设置的密钥库的访问密码
	 */
	private String keyStorePwd;
	/**
	 * 系统的统一识别码
	 */
	private String onlykey;
	/**
	 * 证书路径
	 */
	private String licName;
	/**
	 * 公钥库路径
	 */
	private String pubPath;
	private String confPath = "/licenseVertifyConf.properties";

	public LicenseVerifier(String onlykey) {  
        setConf(confPath,onlykey);  
    }

	public LicenseVerifier(String confPath,String onlykey) {  
        setConf(confPath,onlykey);  
    }

	/**
	 * 功能描述：通过外部配置文件获取配置信息.
	 * 
	 * @param confPath 配置文件路径.
	 * @param onlykey 系统的统一识别码.
	 */
	public void setConf(String confPath, String onlykey) {
		// 获取参数
		Properties prop = new Properties();
		InputStream in = getClass().getResourceAsStream(confPath);
		try {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.onlykey = onlykey;
		pubAlias = prop.getProperty("public.alias");
		keyStorePwd = prop.getProperty("key.store.pwd");
		licName = prop.getProperty("license.name");
		pubPath = "/" + prop.getProperty("public.store.path");

	}

	/**
	 * 功能描述：初始化证书的相关参数.
	 * 
	 * @param 系统的统一识别码.
	 * @return
	 */
	private LicenseParam initLicenseParams() {
		Class<LicenseVerifier> clazz = LicenseVerifier.class;
		Preferences pre = Preferences.userNodeForPackage(clazz);
		CipherParam cipherParam = new DefaultCipherParam(keyStorePwd);
		KeyStoreParam pubStoreParam = new DefaultKeyStoreParam(clazz, pubPath, pubAlias, keyStorePwd, null);
		LicenseParam licenseParam = new DefaultLicenseParam(onlykey, pre, pubStoreParam, cipherParam);
		return licenseParam;
	}

	private LicenseManager getLicenseManager() {
		return LicenseManagerHolder.getLicenseManager(initLicenseParams());
	}

	/**
	 * 功能描述：安装证书证书.
	 * 
	 * @param licdir 存放证书的路径.
	 * @return
	 * @throws Exception e.
	 */
	public void install(String licdir) throws Exception {
		try {
			LicenseManager licenseManager = getLicenseManager();
			licenseManager.install(new File(licdir + licName));
			log.info("安装证书成功!");
		} catch (Exception e) {
			log.info("安装证书失败!");
			e.printStackTrace();
			throw e;
		}

	}
	
	/**
	 * 功能描述：安装证书证书.
	 * 
	 * @param licFile 证书文件.
	 * @return
	 * @throws Exception e.
	 */
	public void install(File licFile) throws Exception {
		try {
			LicenseManager licenseManager = getLicenseManager();
			licenseManager.install(licFile);
			log.info("安装证书成功!");
		} catch (Exception e) {
			log.info("安装证书失败!");
			e.printStackTrace();
			throw e;
		}

	}
	
	/**
	 * 功能描述：验证证书的合法性.
	 * 
	 * @return 0、合法，1、证书过期，2、证书错误
	 */
	public int vertify() {
		try {
			LicenseManager licenseManager = getLicenseManager();
			licenseManager.verify();
			log.info("验证证书成功！");
			return 0;
		} catch (LicenseContentException ex) {
			log.info("证书已经过期！");
			ex.printStackTrace();
			return 1;
		} catch (Exception e) {
			log.info("验证证书失败！");
			e.printStackTrace();
			return 2;
		}
	}
}
