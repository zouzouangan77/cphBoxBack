package com.psu.rouen.cphbox;

import com.psu.rouen.cphbox.CphBoxBackApp;
import com.psu.rouen.cphbox.config.AsyncSyncConfiguration;
import com.psu.rouen.cphbox.config.EmbeddedElasticsearch;
import com.psu.rouen.cphbox.config.EmbeddedMongo;
import com.psu.rouen.cphbox.config.EmbeddedRedis;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { CphBoxBackApp.class, AsyncSyncConfiguration.class })
@EmbeddedRedis
@EmbeddedMongo
@EmbeddedElasticsearch
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public @interface IntegrationTest {
}
