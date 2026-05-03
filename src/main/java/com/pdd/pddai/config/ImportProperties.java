package com.pdd.pddai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "import.questions")
@Data
public class ImportProperties {
    private String file;
}
