package cn.com.vans;

import cn.com.vans.Db.JDBCTools;
import cn.com.vans.Db.SysDocFileSQL;
import cn.com.vans.filetool.ftp.FtpUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;

@Controller
public class UpFileController {

    @RequestMapping(path = {"/uploadFile"})
    public String uploadFile (@RequestParam("localPath")String localPath, ModelMap map) throws Exception {
        String filePath=localPath;
        System.out.println("localPath:"+localPath);
        File file = new File(filePath);
        String fileType = FilenameUtils.getExtension(file.getName());
        String uuid = SysDocFileSQL.getUUID();
        String executeSql = insertSysDocFileSql(file,uuid,fileType);
        JDBCTools.execute(executeSql);
        FtpUtil.uploadFile("/temp/downloadFile/",file.getName(),new FileInputStream(file));
        System.out.println("数据库UUID:[  "+ uuid+" ]");
        map.put("uuid",uuid);
        return "index";
    }



    /**
     * 获取可执行的sql 对mysql数据库置值
     * @param file
     * @param uuid
     * @param fileType
     * @return
     */
    private  static String insertSysDocFileSql(File file ,String uuid,String fileType){
        Object[] objects = new Object[]{
                "'"+uuid+"'", "'"+file.getName()+"'" ,"'"+file.length()+"'",  "'/temp/downloadFile/"+file.getName()+"'",   "'"+fileType+"'", "'1'"

        };
        return MessageFormat.format(SysDocFileSQL.INSERT_SYSDOCFILE,objects);
    }

}
