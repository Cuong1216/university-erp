package com.wiz.universityerpapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.write.jdbc-url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.write.driver-class-name=org.h2.Driver",
    "spring.datasource.write.username=sa",
    "spring.datasource.write.password=",
    "spring.datasource.read.jdbc-url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.read.driver-class-name=org.h2.Driver",
    "spring.datasource.read.username=sa",
    "spring.datasource.read.password=",
    "ai.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "ai.datasource.driver-class-name=org.h2.Driver",
    "ai.datasource.username=sa",
    "ai.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never"
})
class UniversityErpApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
