package pl.edu.amu.wmi;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan
@EnableJpaRepositories("pl.edu.amu.wmi.dao")
public class PersistenceConfiguration {

}
