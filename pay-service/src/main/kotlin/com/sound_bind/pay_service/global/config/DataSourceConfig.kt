package com.sound_bind.pay_service.global.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.sql.SQLException
import javax.sql.DataSource

@Configuration
class DataSourceConfig {

    @Bean(MASTER_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.master.hikari")
    fun masterDatabase(): DataSource =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()

    @Bean(SLAVE_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.slave.hikari")
    fun slaveDatabase(): DataSource =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()

    @Bean(ROUTING_DATASOURCE)
    @Primary
    @DependsOn(MASTER_DATASOURCE, SLAVE_DATASOURCE)
    @Throws(SQLException::class)
    fun routingDataSource(
        @Qualifier(MASTER_DATASOURCE) masterDatabase: DataSource,
        @Qualifier(SLAVE_DATASOURCE) slaveDatabase: DataSource
    ): DataSource {
        val dataSourceMap = mapOf<Any, Any>(
            DataSourceConfigConstants.MASTER_DATASOURCE to masterDatabase,
            DataSourceConfigConstants.SLAVE_DATASOURCE to slaveDatabase
        )
        return RoutingDataSource().apply {
            setTargetDataSources(dataSourceMap)
            setDefaultTargetDataSource(masterDatabase)
        }
    }

    @Bean(LAZY_DATASOURCE)
    @DependsOn(ROUTING_DATASOURCE)
    fun lazyDataSource(routingDataSource: DataSource): LazyConnectionDataSourceProxy =
        LazyConnectionDataSourceProxy(routingDataSource)

    class RoutingDataSource: AbstractRoutingDataSource() {
        override fun determineCurrentLookupKey(): Any {
            val isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            return if (isReadOnly) DataSourceConfigConstants.SLAVE_DATASOURCE
                else DataSourceConfigConstants.MASTER_DATASOURCE
        }
    }

    companion object {
        private const val MASTER_DATASOURCE = "masterDataSource"
        private const val SLAVE_DATASOURCE = "slaveDataSource"
        private const val ROUTING_DATASOURCE = "routingDataSource"
        private const val LAZY_DATASOURCE = "lazyDataSource"
    }

    private enum class DataSourceConfigConstants {
        MASTER_DATASOURCE,
        SLAVE_DATASOURCE
    }
}