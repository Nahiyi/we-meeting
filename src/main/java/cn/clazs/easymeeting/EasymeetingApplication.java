package cn.clazs.easymeeting;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Slf4j
@EnableAsync
@EnableScheduling
@MapperScan("cn.clazs.easymeeting.mapper")
public class EasymeetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasymeetingApplication.class, args);
        log.info(">>>>>>>>>>>>>>> Server Started");
    }

}
