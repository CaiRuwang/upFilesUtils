package cn.com.vans.Db;

import java.util.UUID;

public class SysDocFileSQL {

    public static final String INSERT_SYSDOCFILE="INSERT INTO `sys_doc_file`" +
            " (`id`,`file_name`, `file_size`, `file_path`, `file_type`, " +
            "`is_deleted`, `create_user_id`, `create_time`,  " +
            "`is_ftp_file`, `file_nums`, `file_md5`, `is_temp_file`)" +
            "VALUES" +
            "({0}, {1}, {2}, {3},{4} , " +
            "'0', -1, EBS_SYSDATE (), " +
            "{5},'1', NULL, '0');";

    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
