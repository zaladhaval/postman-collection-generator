package com.collection.creator.autoconfigure;

import com.collection.creator.config.PostmanCollectionProperties;
import com.collection.creator.service.PostmanCollectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Auto-configuration for Postman Collection Generator.
 *
 * <p>This configuration is automatically activated when:
 * <ul>
 *   <li>The library is on the classpath</li>
 *   <li>The application is a web application</li>
 *   <li>The property {@code postman.collection.generator.enabled} is true (default)</li>
 * </ul>
 *
 * <p>To disable the auto-configuration, set:
 * <pre>
 * postman.collection.generator.enabled=false
 * </pre>
 *
 * <p>The library provides only the {@link PostmanCollectionService} bean for programmatic access.
 * Inject the service into your own controllers or services to generate Postman collections:
 * <pre>
 * {@code @Autowired}
 * private PostmanCollectionService collectionService;
 *
 * public void generateCollection() {
 *     String outputPath = collectionService.generateCollection("http://localhost:8080");
 * }
 * </pre>
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass({RequestMappingHandlerMapping.class, ObjectMapper.class})
@ConditionalOnProperty(
    prefix = "postman.collection.generator",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(PostmanCollectionProperties.class)
public class PostmanCollectionAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PostmanCollectionAutoConfiguration.class);

    public PostmanCollectionAutoConfiguration() {
        logger.info("Postman Collection Generator Auto-Configuration activated");
    }

    /**
     * Create the ObjectMapper bean if not already present.
     * This ensures Jackson is available for JSON serialization.
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Create the PostmanCollectionService bean.
     *
     * <p>This service can be injected and used programmatically by the host application
     * to generate Postman collections from REST endpoints.
     *
     * <p>Example usage:
     * <pre>
     * {@code @Autowired}
     * private PostmanCollectionService collectionService;
     *
     * public String generateCollection() {
     *     return collectionService.generateCollection("http://localhost:8080");
     * }
     * </pre>
     */
    @Bean
    @ConditionalOnMissingBean
    public PostmanCollectionService postmanCollectionService(
            ApplicationContext applicationContext,
            PostmanCollectionProperties properties,
            ObjectMapper objectMapper) {
        logger.info("Creating PostmanCollectionService bean for programmatic access");
        return new PostmanCollectionService(applicationContext, properties, objectMapper);
    }
}

