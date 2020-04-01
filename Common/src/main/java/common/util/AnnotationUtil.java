package common.util;

import config.ReflectionsConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

public class AnnotationUtil {
    private Reflections reflections;
    private final ReflectionsConfiguration configuration;
    private static final Logger log = LogManager.getLogger(AnnotationUtil.class.getName());
    private static final String INCLUDE = "include_package";
    private static final String INCLUDE_REGEX = "include_regex";


    private static AnnotationUtil instance = new AnnotationUtil();

    private AnnotationUtil() {
        configuration = new ReflectionsConfiguration();
        configure();
    }

    public static AnnotationUtil UTIL() {
        if (instance == null) {
            instance = new AnnotationUtil();
        }
        return instance;
    }

    void configure() {
        final FilterBuilder filterBuilder = new FilterBuilder();
        final ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        Map<String, List<String>> configuration = this.configuration.getConfiguration();
        for (Map.Entry<String, List<String>> entry : configuration.entrySet()) {
            if (entry.getValue() != null) {
                for (String v : entry.getValue()) {
                    if (!v.equals("")) {
                        if (entry.getKey().equals(INCLUDE) || entry.getKey().equals(INCLUDE_REGEX)) {
                            log.debug("include package: {}", v);
                            configurationBuilder.addUrls(ClasspathHelper.forPackage(v));
                            filterBuilder.include(FilterBuilder.prefix(v));
                        } else {
                            log.debug("exclude package: {}", v);
                            filterBuilder.exclude(FilterBuilder.prefix(v));
                        }
                    }
                }
            }
        }

        configurationBuilder.filterInputsBy(filterBuilder)
                .setScanners(
                        new SubTypesScanner(false),
                        new TypeAnnotationsScanner(),
                        new MethodAnnotationsScanner());

        reflections = new Reflections(configurationBuilder);
    }

    public Set<Method> getMethodsAnnotation(Class<? extends Annotation> annotation) {
        return reflections.getMethodsAnnotatedWith(annotation);
    }

    public Set<Class<?>> getClassAnnotation(Class<? extends Annotation> annotation) {
        return reflections.getTypesAnnotatedWith(annotation);
    }

    public Reflections getReflections() {
        return reflections;
    }
}
