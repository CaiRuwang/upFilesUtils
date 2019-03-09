package cn.com.vans.filetool.ftp;


import cn.com.vans.spring.BeanFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FtpUtil {
	private static Logger logger = LoggerFactory.getLogger(FtpUtil.class);

	private static Environment env = BeanFactory.getBean(Environment.class);

	private static String Ftp_Host = env.getProperty("ftp.Ftp_Host");

	private static String Ftp_Username = env.getProperty("ftp.Ftp_Username");

	private static String FTP_Password = env.getProperty("ftp.FTP_Password");

	private static String Ftp_Defart_Dir = env.getProperty("ftp.Ftp_Defart_Dir");

	private static String Location_Default_Dir = env.getProperty("file.Location_Default_Dir");

	private static int Ftp_Port = Integer.parseInt(env.getProperty("ftp.Ftp_Port"));
	private static final String SEPARATOR = "/";



	public static  FTPClient getFtpClient() throws Exception {

		try {
			FTPClient ftpClient = new FTPClient();
			ftpClient.setDefaultTimeout(10 * 1000);
			ftpClient.setConnectTimeout(10 * 1000);
			ftpClient.setDataTimeout(30 * 1000);
			ftpClient.setDefaultPort(Ftp_Port);
			ftpClient.connect(Ftp_Host);

			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				disconnect(ftpClient);
				throw new IOException("Can't connect to server '" + Ftp_Port + "'");
			}

			// Login.
			if (!ftpClient.login(Ftp_Username, FTP_Password)) {
				disconnect(ftpClient);
				throw new IOException("Can't login to server '" + Ftp_Port + "'");
			}

			logger.info("FTP 已连接!");
			return ftpClient;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	private static void disconnect(FTPClient ftpClient) {
		try {
			if (ftpClient != null && ftpClient.isConnected()) {
				ftpClient.logout();
				ftpClient.disconnect();
				ftpClient = null;
				logger.info("FTP 断开连接!");
			}
		} catch (Exception e) {
			logger.error("FTP 断开连接错误!", e);
		}
	}

	public static void uploadFile(String desFileFullName, File file) throws Exception {
		try {
			uploadFile(desFileFullName, new FileInputStream(file));
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new Exception(e);
		}
	}

	public static void uploadFile(File srcFile) throws Exception {

		String desFilePath = srcFile.getAbsolutePath();
		uploadFile(desFilePath, new FileInputStream(srcFile));
	}

	public static String uploadFile(String desFilePath, InputStream in) throws Exception {

		File file = new File(desFilePath);
		String desFileDir = file.getParent() != null ? file.getParent() : ".";
		String desFileName = file.getName();
		desFileName = new String(desFileName.getBytes("gbk"), "ISO-8859-1");
		desFileDir = new String(desFileDir.getBytes("gbk"), "ISO-8859-1");

		uploadFile(desFileDir, desFileName, in);
		return "";
	}

	public static void uploadFile(String desFileDir, String desFileName, File file) {
		try {
			uploadFile(desFileDir, desFileName, new FileInputStream(file));
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public static void uploadFile(String desFileDir, String desFileName, InputStream fileIn) throws Exception {
		logger.info("FTP上传：存储目录 = " + desFileDir + ", 文件名=" + convertCharSet(desFileName));
		FTPClient ftpClient = null;
		try {
			ftpClient = getFtpClient();
			desFileDir = fixFileName(desFileDir);
			boolean changeDirOk = ftpClient.changeWorkingDirectory(Ftp_Defart_Dir);
			if (desFileDir != null && desFileDir.length() > 0 && !".".equals(desFileDir)) {
				changeDirOk = ftpClient.changeWorkingDirectory(desFileDir);
				if (!changeDirOk) {
					makeDirs(ftpClient, desFileDir);
				}
			}
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			// Use passive mode to pass firewalls.
			ftpClient.enterLocalPassiveMode();
			boolean uploadOk = ftpClient.storeFile(desFileName, fileIn);
			if (!uploadOk)
				throw new Exception("上传 " + desFileDir + SEPARATOR + convertCharSet(desFileName) + " 失败!");
			logger.info("上传成功!");
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new Exception(e);
		} finally {
			IOUtils.closeQuietly(fileIn);
			disconnect(ftpClient);
		}
	}

	public  static File downloadFile(String fileFullName, String saveFileName) throws Exception {

		File file = new File(saveFileName);
		try {
			if (!file.exists()) {
				File parentFile = file.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
			}
			downloadFile(fileFullName, new FileOutputStream(file));
			return file;
		} catch (Exception e) {
			logger.error(e.getMessage());
			try {
				if (file.exists() && file.length() == 0) {
					file.delete();
				}
			} catch (Exception e1) {
			}
			throw new Exception(e);
		}
	}


	public static void downloadFile(String filePathName, OutputStream output) throws Exception {

		filePathName = new String(filePathName.getBytes("gbk"), "ISO-8859-1");
		FTPClient ftpClient = null;
		logger.info("FTP下载：文件全名 = " + convertCharSet(filePathName));
		try {
			ftpClient = getFtpClient();

			// Use passive mode to pass firewalls.
		 	ftpClient.enterLocalPassiveMode();

			filePathName = fixFileName(filePathName);
			ftpClient.changeWorkingDirectory(Ftp_Defart_Dir);
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

			boolean downloadOk = ftpClient.retrieveFile(filePathName, output);

			if (!downloadOk)
				throw new Exception("下载 " + convertCharSet(filePathName) + " 失败!");
			logger.info("下载成功!");
		} catch (Exception e) {
			logger.error("从ftp下载文件失败",e);
			throw new Exception(e);
		} finally {
			IOUtils.closeQuietly(output);
			disconnect(ftpClient);
		}
	}

	private static void makeDirs(FTPClient ftpClient, String dirName) throws Exception {

		File dir = new File(dirName);
		String currentDirName = dir.getName();
		if (dir.getParent() != null) {
			makeDirs(ftpClient, dir.getParent());
		}
		makeDir(ftpClient, currentDirName);
	}

	private static void makeDir(FTPClient ftpClient, String dirName) throws Exception {

		try {
			boolean changeDirOk = ftpClient.changeWorkingDirectory(dirName);
			if (!changeDirOk) {
				ftpClient.makeDirectory(dirName);
				changeDirOk = ftpClient.changeWorkingDirectory(dirName);
			}
			if (!changeDirOk)
				throw new Exception("创建目录" + dirName + "失败！");
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * 修正不规范的文件名:不能从根目录开头,应以/为目录分隔符.
	 * 
	 * @param fileName
	 * @return
	 */
	private static String fixFileName(String fileName) {

		if (fileName != null) {
			File file = new File(fileName);
			fileName = file.getPath();
			fileName = StringUtils.replace(fileName, "\\", SEPARATOR);
			File file2 = new File(Location_Default_Dir);
			String fileName2 = file2.getPath();
			fileName2 = StringUtils.replace(fileName2, "\\", SEPARATOR);
			fileName = fileName.replaceFirst(fileName2, "");

			while (fileName.startsWith(SEPARATOR)) {
				fileName = StringUtils.substring(fileName, 1);
			}
		}
		return fileName;
	}

	public static String convertCharSet(String str) {

		if (str == null || str.length() == 0) {
			return str;
		}

		try {
			return new String(str.getBytes("ISO-8859-1"), "GBK");
		} catch (Exception e) {
			logger.error("转换错误!", e);
			return str;
		}
	}

	public static void downloadFile(String filePathName,File file) throws Exception {
		if (!file.exists()) {
			File parentFile = file.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
		}
		downloadFile(filePathName,new FileOutputStream(file));
	}

	/**
	 * 删除目录下所有文件
	 * 
	 * @param remotePath
	 * @throws Exception
	 */
	public void deleteFtpDirectory(String remotePath) throws Exception {
		FTPClient ftpClient = getFtpClient();
		ftpClient.changeWorkingDirectory(remotePath);
		FTPFile[] files = ftpClient.listFiles();
		for (int i = 0; i < files.length; i++) {
			ftpClient.deleteFile(remotePath + SEPARATOR + files[i].getName());
//			System.out.println(files[i].getName());
		}
	}

	/**
	 * 删除目录下某个文件
	 *
	 * @param filePath
	 * @throws Exception
	 */
	public  boolean deleteFile(String filePath){
		FTPClient ftpClient = null;
		boolean success = false;
		try {
			File f = new File(filePath);
			ftpClient = getFtpClient();
//			ftpClient.changeWorkingDirectory(Ftp_Defart_Dir+SEPARATOR+f.getPath());
//			success = ftpClient.deleteFile(f.getName());
			success = ftpClient.deleteFile(Ftp_Defart_Dir+SEPARATOR+f.getPath());
		}catch (Exception e){
			logger.error("删除ftp中的临时文件失败，文件路径："+filePath);
		}finally {
			disconnect(ftpClient);
		}
		return success;
	}

	/**
	 * 处理特殊的字符串 用"_"代替"\/:*?<>|"
	 * 
	 * @param fileName
	 * @return
	 */
	public static final String filteName(String fileName) {
		return fileName.replaceAll("[?*<>:|/\\\\]", "_").trim();
	}

}
