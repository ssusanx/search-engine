package org.search.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@SpringBootApplication
public class SearchQueryApplication {

	public static void main(String[] args) {
		SpringApplication.run(SearchQueryApplication.class, args);
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfig() 
	{
		return new PropertySourcesPlaceholderConfigurer();
	}
}
