package org.jfantasy.framework.spring;

import org.jfantasy.framework.util.common.ClassUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClassPathScanner implements ResourceLoaderAware {
    private static final Log LOGGER = LogFactory.getLog(ClassPathScanner.class);
    protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    private static ClassPathScanner instance = new ClassPathScanner();

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    private String resourcePattern = DEFAULT_RESOURCE_PATTERN;

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }

    public static ClassPathScanner getInstance() {
        return instance;
    }

    private final static String CLASSPATH = "classpath*:";

    public Set<String> findTargetClassNames(String basepackage) {
        Set<String> candidates = new LinkedHashSet<String>();
        try {
            String packageSearchPath = CLASSPATH + ClassUtil.convertClassNameToResourcePath(basepackage) + "/" + this.resourcePattern;
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                String clazzName = metadataReader.getClassMetadata().getClassName();
                candidates.add(clazzName);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Find Class : " + clazzName);
                }
            }
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
        }
        return candidates;
    }

    /**
     * 查找Class 根据是否标注指定的注解
     *
     * @param <T>         注解泛型
     * @param basepackage 扫描路径
     * @param anno        注解
     * @return 标注注解的Class
     */
    public <T extends Annotation> Set<Class> findAnnotationedClasses(String basepackage, Class<T> anno) {
        Set<Class> candidates = new LinkedHashSet<Class>();
        try {
            String packageSearchPath = "classpath*:" + ClassUtil.convertClassNameToResourcePath(basepackage) + "/" + this.resourcePattern;
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                if (!metadataReader.getAnnotationMetadata().hasAnnotation(anno.getName())) {
                    continue;
                }
                try {
                    String clazzName = metadataReader.getClassMetadata().getClassName();
                    candidates.add(Class.forName(clazzName));
                    LOGGER.debug("Find Annotationed Class " + clazzName + "(@" + anno.getName() + ")");
                } catch (ClassNotFoundException ignored) {
                    LOGGER.error(ignored.getMessage(),ignored);
                }
            }
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);//NOSONAR
        }
        return candidates;
    }

    public String getResourcePattern() {
        return this.resourcePattern;
    }

    public void setResourcePattern(String resourcePattern) {
        this.resourcePattern = resourcePattern;
    }

    /**
     * @param basepackage    扫描包
     * @param interfaceClass 接口或者父类
     * @return class
     */
    public <T> Set<Class> findInterfaceClasses(String basepackage, Class<T> interfaceClass) {

        Set<Class> candidates = new LinkedHashSet<Class>();
        try {
            String packageSearchPath = "classpath*:" + ClassUtil.convertClassNameToResourcePath(basepackage) + "/" + this.resourcePattern;
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                ClassMetadata classMetadata = metadataReader.getClassMetadata();
                if (classMetadata.isInterface()) {
                    continue;
                }
                String clazzName = metadataReader.getClassMetadata().getClassName();
                try {
                    Class<?> clazz = Class.forName(clazzName);
                    if (interfaceClass.isAssignableFrom(clazz)) {
                        candidates.add(clazz);
                    }
                } catch (ClassNotFoundException localClassNotFoundException) {
                    LOGGER.error(localClassNotFoundException.getMessage(), localClassNotFoundException);
                } catch (NoClassDefFoundError e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
        }
        return candidates;
    }

}