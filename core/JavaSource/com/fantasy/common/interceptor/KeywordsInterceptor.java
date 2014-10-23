package com.fantasy.common.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.wltea.analyzer.dic.Dictionary;

import com.fantasy.framework.util.common.ClassUtil;
import com.fantasy.framework.util.common.StringUtil;

/**
 * 扩展关键字操作
 * 
 * @功能描述
 * @author 李茂峰
 * @since 2013-12-24 上午10:14:31
 * @version 1.0
 */
public class KeywordsInterceptor {

	private static final Logger logger = Logger.getLogger(KeywordsInterceptor.class);

	public void reloadDictionary() {
		Dictionary dictionary = Dictionary.getInstance();
		try {
			Method method = ClassUtil.getDeclaredMethod(Dictionary.class, "loadMainDict");
			method.invoke(dictionary);
			method = ClassUtil.getDeclaredMethod(Dictionary.class, "loadSurnameDict");
			method.invoke(dictionary);
			method = ClassUtil.getDeclaredMethod(Dictionary.class, "loadQuantifierDict");
			method.invoke(dictionary);
			method = ClassUtil.getDeclaredMethod(Dictionary.class, "loadSuffixDict");
			method.invoke(dictionary);
			method = ClassUtil.getDeclaredMethod(Dictionary.class, "loadPrepDict");
			method.invoke(dictionary);
			method = ClassUtil.getDeclaredMethod(Dictionary.class, "loadStopWordDict");
			method.invoke(dictionary);
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void addExtendWords(JoinPoint point) {
		String keywords = (String) point.getArgs()[0];
		if (StringUtil.isBlank(keywords)) {
			return;
		}
		List<String> list = new ArrayList<String>();
		list.add(keywords);
		Dictionary.loadExtendWords(list);
//		Dictionary.loadExtendStopWords(extStopWords);
	}

	public void removeExtendWords(JoinPoint point) {
	}

}