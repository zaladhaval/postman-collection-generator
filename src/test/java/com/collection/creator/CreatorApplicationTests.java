package com.collection.creator;

import com.collection.creator.autoconfigure.PostmanCollectionAutoConfiguration;
import com.collection.creator.config.PostmanCollectionProperties;
import com.collection.creator.service.PostmanCollectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for Postman Collection Generator Auto-Configuration.
 */
@SpringBootTest(classes = CreatorApplicationTests.TestApplication.class)
class CreatorApplicationTests {

	@Autowired(required = false)
	private PostmanCollectionService postmanCollectionService;

	@Autowired(required = false)
	private PostmanCollectionProperties properties;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		assertThat(applicationContext).isNotNull();
	}

	@Test
	void autoConfigurationShouldLoadService() {
		assertThat(postmanCollectionService).isNotNull();
	}

	@Test
	void autoConfigurationShouldLoadProperties() {
		assertThat(properties).isNotNull();
		assertThat(properties.isEnabled()).isTrue();
	}

	@Test
	void propertiesShouldHaveDefaultValues() {
		assertThat(properties.getCollection().getName()).isEqualTo("API Collection");
		assertThat(properties.getOutput().getFilename()).isEqualTo("collection.json");
		assertThat(properties.getAuthorization().isEnabled()).isTrue();
		assertThat(properties.getBaseUrl()).isEmpty();
	}

	/**
	 * Minimal Spring Boot application for testing.
	 */
	@SpringBootApplication
	static class TestApplication {
		// Minimal test application
	}
}
