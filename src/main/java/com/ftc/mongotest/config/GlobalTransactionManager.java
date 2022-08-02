package com.ftc.mongotest.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author: 冯铁城 [17615007230@163.com]
 * @date: 2022-07-29 16:53:06
 * @describe:
 */
@Configuration
public class GlobalTransactionManager {

    @Bean(name = "transactionManagerConfig")
    public ChainedTransactionManager transactionManagerConfig(
            @Qualifier("primaryMongoTransactionManager") PlatformTransactionManager ptm1,
            @Qualifier("secondaryMongoTransactionManager") PlatformTransactionManager ptm2) {
        return new ChainedTransactionManager(ptm1, ptm2);
    }
}
