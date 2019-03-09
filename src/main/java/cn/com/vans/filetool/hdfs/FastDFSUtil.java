/**
 *
 */
package cn.com.vans.filetool.hdfs;
import cn.com.vans.spring.BeanFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.DownloadStream;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.csource.fastdfs.UploadStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author vans
 *
 */

public class FastDFSUtil {
    private static  Logger logger = LoggerFactory.getLogger(FastDFSUtil.class);
    private static Properties properties;
    private static final String PROP_KEY_CONNECT_TIMEOUT_IN_SECONDS="fastdfs.connect_timeout";
    private static final String PROP_KEY_NETWORK_TIMEOUT_IN_SECONDS="fastdfs.network_timeout";
    private static final String PROP_KEY_CHARSET="fastdfs.charset";
    private static final String PROP_KEY_HTTP_TRACKER_HTTP_PORT="fastdfs.http.tracker_http_port";
    private static final String PROP_KEY_HTTP_ANTI_STEAL_TOKEN="fastdfs.http.anti_steal_token";
    private static final String PROP_KEY_HTTP_SECRET_KEY="fastdfs.http.secret_key";
    private static final String PROP_KEY_TRACKER_SERVERS="fastdfs.tracker_servers";

    private static Environment env = BeanFactory.getBean(Environment.class);

    static{
        properties=new Properties();
        List<String> l = new ArrayList<>();
        l.add(PROP_KEY_CONNECT_TIMEOUT_IN_SECONDS);
        l.add(PROP_KEY_NETWORK_TIMEOUT_IN_SECONDS);
        l.add(PROP_KEY_CHARSET);
        l.add(PROP_KEY_HTTP_TRACKER_HTTP_PORT);
        l.add(PROP_KEY_HTTP_ANTI_STEAL_TOKEN);
        l.add(PROP_KEY_HTTP_SECRET_KEY);
        l.add(PROP_KEY_TRACKER_SERVERS);
        for(String prop:l){
            setProperties(properties,prop);
        }

    }

    private static void setProperties(Properties properties,String propKey){

        properties.put(propKey,fastDfsMap(propKey));
    }

    public static String uploadFile(File file) {
        FileInputStream in = null;
        TrackerServer trackerServer = null;
        String filePath = "";
        try {
            in = new FileInputStream(file);
            logger.info(properties.toString());
            ClientGlobal.initByProperties(properties);
            String fileExtName = FilenameUtils.getExtension(file.getName());
            // 建立连接
            TrackerClient tracker = new TrackerClient();
            trackerServer = tracker.getConnection();
            StorageServer storageServer = null;
            StorageClient1 client = new StorageClient1(trackerServer, storageServer);
            // 设置元信息
            NameValuePair[] metaList = new NameValuePair[3];
            metaList[0] = new NameValuePair("fileExtName", fileExtName);
            metaList[1] = new NameValuePair("fileLength", String.valueOf(file.length()));
            metaList[2] = new NameValuePair("fileName", getFileNameString(file.getName()));
            filePath = client.upload_file1(null,in.getChannel().size(),new UploadStream(in, file.length()),fileExtName,metaList);
            logger.info("上传文件到fastdfs成功！");
        } catch (Exception e) {

            logger.error("上传文件到fastdfs失败！");
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                if(trackerServer!=null){
                    trackerServer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    public static String uploadFile(String fileName ,InputStream in) {
        TrackerServer trackerServer = null;
        String filePath = "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ClientGlobal.initByProperties(properties);
            String fileExtName = FilenameUtils.getExtension(fileName);
            // 建立连接
            TrackerClient tracker = new TrackerClient();
            trackerServer = tracker.getConnection();
            StorageServer storageServer = null;
            StorageClient1 client = new StorageClient1(trackerServer, storageServer);

            // 设置元信息
            NameValuePair[] metaList = new NameValuePair[3];
            metaList[0] = new NameValuePair("fileExtName", fileExtName);
            metaList[1] = new NameValuePair("fileLength", String.valueOf(in.available()));
            metaList[2] = new NameValuePair("fileName", getFileNameString(fileName));
            // 上传文件
            filePath = client.upload_file1(null,in.available(),new UploadStream(in, in.available()),fileExtName,metaList);
            logger.info("上传文件到fastdfs成功！");
        } catch (Exception e) {
            logger.error("上传文件到fastdfs失败！");
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(bos);
            try {
                if(trackerServer!=null){
                    trackerServer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    public static void downloadFile(String filePathName,File file) throws FileNotFoundException {
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
        }
        downloadFile(filePathName,new FileOutputStream(file));
    }

    public static void downloadFile(String filePathName, OutputStream output){
        TrackerServer trackerServer = null;
        try {
            ClientGlobal.initByProperties(properties);
            TrackerClient tracker = new TrackerClient();
            trackerServer = tracker.getConnection();
            StorageServer storageServer = null;
            StorageClient1 client = new StorageClient1(trackerServer, storageServer);
            client.download_file1(filePathName,new DownloadStream(output));
        } catch (Exception e) {
            logger.error("从fastdfs下载文件失败",e);
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(output);
            try {
                if(trackerServer!=null){
                    trackerServer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteFile(String filePathName){
        boolean success = false;
        try {
            ClientGlobal.initByProperties(properties);
            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;

            StorageClient storageClient = new StorageClient(trackerServer,
                    storageServer);
            String group_name = filePathName.split("/")[0];
            String remote_filename = filePathName.substring(group_name.length()+1);
            int i = storageClient.delete_file(group_name, remote_filename);
            if(i==0){
                logger.info("已从fastdfs上删除文件");
                success = true;
            }else{
                logger.warn("fastdfs上已没有该文件");
                success = false;
            }

        } catch (Exception e) {
            logger.warn("从fastdfs上删除文件失败");
        }
        return success;
    }

    private static String getFileNameString(String fileName){
        String str1="";
        if(fileName.indexOf(".")!=-1){
            str1=fileName.substring(0, fileName.lastIndexOf("."));
        }else{
            str1=fileName;
        }
        return str1;
    }

    private static String fastDfsMap(String propKey){
        Map<String,String> map = new HashMap<>();
        map.put("fastdfs.connect_timeout","600");
        map.put("fastdfs.network_timeout","1200");
        map.put("fastdfs.charset","UTF-8");
        map.put("fastdfs.http.tracker_http_port","8888");
        map.put("fastdfs.http.anti_steal_token","false");
        map.put("fastdfs.http.secret_key","FastDFS1234567890");
        map.put("fastdfs.tracker_servers","106.75.216.203:33308");
        return map.get(propKey);

    }
}
