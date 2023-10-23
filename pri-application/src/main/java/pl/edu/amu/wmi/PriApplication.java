package pl.edu.amu.wmi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.edu.amu.wmi.config.PriConfigProperties;

@SpringBootApplication
@EnableConfigurationProperties(PriConfigProperties.class)
public class PriApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriApplication.class, args);
    }

}
