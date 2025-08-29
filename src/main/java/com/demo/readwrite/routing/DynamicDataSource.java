package com.demo.readwrite.routing;

import com.demo.readwrite.enums.DataSourceType;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {
    
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceType = DataSourceContextHolder.getDataSource();
        if (dataSourceType == null) {
            // 默认使用从库
            return DataSourceType.SLAVE.getValue();
        }
        return dataSourceType.getValue();
    }
}