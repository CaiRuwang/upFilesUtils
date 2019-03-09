package cn.com.vans;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"cn.com.vans"})
public class UpFilesApplication {

    public static void main(String args[]){
        SpringApplication.run(UpFilesApplication.class);
    }
}
