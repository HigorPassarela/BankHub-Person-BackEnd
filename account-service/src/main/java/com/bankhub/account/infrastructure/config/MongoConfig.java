package com.bankhub.account.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // A anotação @EnableMongoAuditing instrui o Spring Data a preencher
    // automaticamente as propriedades anotadas com @CreatedDate e @LastModifiedDate.
}
