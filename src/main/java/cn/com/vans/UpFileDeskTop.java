package cn.com.vans;

import cn.com.vans.filetool.hdfs.FastDFSUtil;

import java.io.File;

public class UpFileDeskTop {

    public static void  main(String args[]){

        String filePath="/Users/cairuwang/Downloads/cfg_proc_def_node.sql";
        File file = new File(filePath);
        String fileId=FastDFSUtil.uploadFile(file);
        System.out.println(fileId);
    }


}
