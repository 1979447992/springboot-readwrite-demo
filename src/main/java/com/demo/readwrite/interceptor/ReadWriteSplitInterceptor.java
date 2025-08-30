package com.demo.readwrite.interceptor;

import com.demo.readwrite.strategy.SimpleReadWriteStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * MyBatis读写分离拦截器
 * 自动识别SQL类型并设置路由策略
 */
//@Component
@Intercepts({
    @Signature(type = Executor.class, method = "update", 
               args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", 
               args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Slf4j
public class ReadWriteSplitInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];

        // 获取SQL命令类型
        SqlCommandType sqlCommandType = ms.getSqlCommandType();
        
        // 获取实际SQL语句
        BoundSql boundSql = ms.getBoundSql(parameter);
        String sql = boundSql.getSql();
        
        // 检查是否已有强制设置（如@MasterOnly）
        SimpleReadWriteStrategy.SqlType existingSqlType = SimpleReadWriteStrategy.getSqlType();
        SimpleReadWriteStrategy.SqlType sqlType;
        
        if (existingSqlType != null) {
            // 已有强制设置，不覆盖
            sqlType = existingSqlType;
            log.debug("检测到强制路由设置，保持: {}", sqlType);
        } else {
            // 自动判断SQL类型并设置路由策略
            sqlType = determineSqlType(sqlCommandType, sql);
            SimpleReadWriteStrategy.setSqlType(sqlType);
        }
        
        log.debug("SQL拦截 - 类型: {}, 路由: {}, SQL: {}", 
                 sqlCommandType, sqlType, sql.replaceAll("\\s+", " ").trim());

        return invocation.proceed();
    }

    /**
     * 判断SQL类型
     */
    private SimpleReadWriteStrategy.SqlType determineSqlType(SqlCommandType sqlCommandType, String sql) {
        // 明确的写操作
        if (SqlCommandType.INSERT == sqlCommandType || 
            SqlCommandType.UPDATE == sqlCommandType || 
            SqlCommandType.DELETE == sqlCommandType) {
            return SimpleReadWriteStrategy.SqlType.WRITE;
        }
        
        // SELECT语句需要进一步判断
        if (SqlCommandType.SELECT == sqlCommandType) {
            return analyzeSelectStatement(sql);
        }
        
        // 其他情况（如存储过程调用等）默认走主库保证安全
        return SimpleReadWriteStrategy.SqlType.WRITE;
    }

    /**
     * 分析SELECT语句是否需要走主库
     */
    private SimpleReadWriteStrategy.SqlType analyzeSelectStatement(String sql) {
        if (sql == null) {
            return SimpleReadWriteStrategy.SqlType.WRITE;
        }
        
        String upperSql = sql.toUpperCase();
        
        // 需要走主库的SELECT场景
        if (upperSql.contains("FOR UPDATE") || 
            upperSql.contains("LOCK IN SHARE MODE") ||
            upperSql.contains("GET_LOCK") ||
            upperSql.contains("MASTER_POS_WAIT") ||
            upperSql.contains("FOUND_ROWS()") ||
            upperSql.contains("ROW_COUNT()") ||
            upperSql.contains("LAST_INSERT_ID()")) {
            return SimpleReadWriteStrategy.SqlType.WRITE;
        }
        
        // 普通SELECT走从库
        return SimpleReadWriteStrategy.SqlType.READ;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以通过properties配置拦截器参数
    }
}
