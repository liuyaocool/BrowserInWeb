package prv.liuyao.BrowserInWeb.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "config")
public class SysConfig {

    private String version;
    private String password;

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
