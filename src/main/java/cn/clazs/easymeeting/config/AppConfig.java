package cn.clazs.easymeeting.config;

import cn.clazs.easymeeting.util.StringTools;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
public class AppConfig {

    /**
     * websocket端口
     */
    @Getter
    @Value("${ws.port:}")
    private Integer wsPort;

    @Value("${project.folder:}")
    private String projectFolder;

    @Getter
    @Value("${admin.emails:}")
    private String adminEmails;

    public String getProjectFolder() {
        if (StringTools.isEmpty(projectFolder) && !projectFolder.endsWith("/")) {
            projectFolder = projectFolder + "/";
        }
        return projectFolder;
    }

}
