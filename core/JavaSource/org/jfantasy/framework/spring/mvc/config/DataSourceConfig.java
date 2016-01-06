/*
 * COPYRIGHT Beijing NetQin-Tech Co.,Ltd.                                   *
 ****************************************************************************
 * 源文件名:  web.config.DataSourceConfig.java 													       
 * 功能: cpframework框架													   
 * 版本:	@version 1.0	                                                                   
 * 编制日期: 2014年9月3日 下午5:24:02 						    						                                        
 * 修改历史: (主要历史变动原因及说明)		
 * YYYY-MM-DD |    Author      |	 Change Description		      
 * 2014年9月3日    |    Administrator     |     Created 
 */
package org.jfantasy.framework.spring.mvc.config;

import org.apache.log4j.Logger;
import org.logicalcobwebs.proxool.ProxoolDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

/** 
 *Description: <类功能描述>. <br>
 *<p>
	<使用说明>
 </p>
 *Makedate:2014年9月3日 下午5:24:02 
 * @author Administrator  
 * @version V1.0                             
 */
@Configuration
//加载资源文件
@PropertySource({"classpath:/props/jdbc.properties"})
public class DataSourceConfig {
	private static final Logger logger = Logger.getLogger(DataSourceConfig.class);
	/*
     * 绑定资源属性
     */
	@Value("${jdbc.driver}")
	private String driverClass;
	@Value("${jdbc.driverUrl}")
	private String url;
	@Value("${jdbc.user}")
	private String userName;
	@Value("${jdbc.password}")
	private String passWord;
	@Value("${jdbc.houseKeepingSleepTime}")
	private String houseKeepingSleepTime;
	@Value("${jdbc.houseKeepingTestSql}")
	private String houseKeepingTestSql;
	@Value("${jdbc.prototypeCount}")
	private String prototypeCount;
	@Value("${jdbc.maximumConnectionCount}")
	private String maximumConnectionCount;
	@Value("${jdbc.minimumConnectionCount}")
	private String minimumConnectionCount;

	@Bean(name = "dataSource")
	public DataSource dataSource() {
		logger.info("DataSource");
		ProxoolDataSource dataSource = new ProxoolDataSource();
		dataSource.setDriver(driverClass);
		dataSource.setDriverUrl(url);
		dataSource.setUser(userName);
		dataSource.setPassword(passWord);
		dataSource.setAlias("portal");
		dataSource.setHouseKeepingSleepTime(Integer.valueOf(houseKeepingSleepTime));
		dataSource.setHouseKeepingTestSql(houseKeepingTestSql);
		dataSource.setPrototypeCount(Integer.valueOf(prototypeCount));
		dataSource.setMaximumConnectionCount(Integer.valueOf(maximumConnectionCount));
		dataSource.setMinimumConnectionCount(Integer.valueOf(minimumConnectionCount));
		dataSource.setTrace(true);
		dataSource.setVerbose(true);
		return dataSource;
	}
}


