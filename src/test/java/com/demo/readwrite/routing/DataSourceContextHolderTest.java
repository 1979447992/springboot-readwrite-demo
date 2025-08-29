package com.demo.readwrite.routing;

import com.demo.readwrite.enums.DataSourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

class DataSourceContextHolderTest {

    @BeforeEach
    void setUp() {
        // 确保每个测试开始前清理上下文
        DataSourceContextHolder.clearDataSource();
    }

    @AfterEach
    void tearDown() {
        // 确保每个测试结束后清理上下文
        DataSourceContextHolder.clearDataSource();
    }

    @Test
    void setMaster_ShouldSetMasterDataSource() {
        DataSourceContextHolder.setMaster();
        
        assertEquals(DataSourceType.MASTER, DataSourceContextHolder.getDataSource());
        assertTrue(DataSourceContextHolder.isMaster());
        assertFalse(DataSourceContextHolder.isSlave());
    }

    @Test
    void setSlave_ShouldSetSlaveDataSource() {
        DataSourceContextHolder.setSlave();
        
        assertEquals(DataSourceType.SLAVE, DataSourceContextHolder.getDataSource());
        assertTrue(DataSourceContextHolder.isSlave());
        assertFalse(DataSourceContextHolder.isMaster());
    }

    @Test
    void setDataSource_ShouldSetCorrectDataSource() {
        DataSourceContextHolder.setDataSource(DataSourceType.MASTER);
        assertEquals(DataSourceType.MASTER, DataSourceContextHolder.getDataSource());
        
        DataSourceContextHolder.setDataSource(DataSourceType.SLAVE);
        assertEquals(DataSourceType.SLAVE, DataSourceContextHolder.getDataSource());
    }

    @Test
    void getDataSource_ShouldReturnNull_WhenNotSet() {
        assertNull(DataSourceContextHolder.getDataSource());
        assertFalse(DataSourceContextHolder.isMaster());
        assertFalse(DataSourceContextHolder.isSlave());
    }

    @Test
    void clearDataSource_ShouldClearCurrentDataSource() {
        DataSourceContextHolder.setMaster();
        assertTrue(DataSourceContextHolder.isMaster());
        
        DataSourceContextHolder.clearDataSource();
        assertNull(DataSourceContextHolder.getDataSource());
        assertFalse(DataSourceContextHolder.isMaster());
        assertFalse(DataSourceContextHolder.isSlave());
    }

    @Test
    void threadLocalIsolation_ShouldWorkCorrectly() throws InterruptedException {
        // 在主线程设置主库
        DataSourceContextHolder.setMaster();
        assertTrue(DataSourceContextHolder.isMaster());

        // 创建新线程并设置从库
        Thread thread = new Thread(() -> {
            DataSourceContextHolder.setSlave();
            assertTrue(DataSourceContextHolder.isSlave());
            assertFalse(DataSourceContextHolder.isMaster());
        });

        thread.start();
        thread.join();

        // 主线程的设置应该不受影响
        assertTrue(DataSourceContextHolder.isMaster());
        assertFalse(DataSourceContextHolder.isSlave());
    }
}