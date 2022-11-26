package utils

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container

@SpringBootTest
abstract class DatabaseContainerConfiguration {
    companion object {

        @Container
        val container = GenericContainer("mongo").apply {
            withExposedPorts(27017)
            withEnv("MONGO_INITDB_ROOT_USERNAME", "rootuser")
            withEnv("MONGO_INITDB_ROOT_PASSWORD", "rootpass")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.host", container::getHost)
            registry.add("spring.data.mongodb.port", container::getFirstMappedPort)
        }
    }
}