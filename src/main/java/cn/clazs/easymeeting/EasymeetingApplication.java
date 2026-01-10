package cn.clazs.easymeeting;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
// @MapperScan("cn.clazs.easymeeting.mappers")
public class EasymeetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasymeetingApplication.class, args);
        log.info(">>>>>>>>>>>>>>> Server Started");
    }

}
