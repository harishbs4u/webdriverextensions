package com.github.webdriverextensions.internal.generator;

import com.github.webdriverextensions.WebSite;
import static com.github.webdriverextensions.internal.generator.GeneratorUtils.annotateFieldWithFindByAnnotation;
import static com.github.webdriverextensions.internal.generator.GeneratorUtils.annotateFieldWithFindBysAnnotation;
import static com.github.webdriverextensions.internal.generator.GeneratorUtils.error;
import static com.github.webdriverextensions.internal.generator.GeneratorUtils.getFieldName;
import static com.github.webdriverextensions.internal.generator.GeneratorUtils.getFindByAnnotation;
import static com.github.webdriverextensions.internal.generator.GeneratorUtils.getFindBysAnnotation;
import static com.github.webdriverextensions.internal.generator.GeneratorUtils.hasFindByAnnotation;
import static com.github.webdriverextensions.internal.generator.GeneratorUtils.hasFindBysAnnotation;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class WebSiteBuilder implements Builder<Boolean> {

    // Input Elements
    final private ProcessingEnvironment processingEnv;
    final private Set<TypeElement> webPageElements;
    final private Set<TypeElement> webRepositoryElements;
    final private Set<TypeElement> otherElements;
    final private JCodeModel codeModel;
    // JClasses
    private JDefinedClass generatedWebSiteClass;

    public WebSiteBuilder(ProcessingEnvironment processingEnv,
            Set<TypeElement> webPageElements, Set<TypeElement> webRepositoryElements, Set<TypeElement> otherElements) {
        this.processingEnv = processingEnv;
        this.webPageElements = webPageElements;
        this.webRepositoryElements = webRepositoryElements;
        this.otherElements = otherElements;
        this.codeModel = new JCodeModel();
    }

    @Override
    public Boolean build() {
        try {
            createClass();
            createFields();
            generate();
            return true;
        } catch (IOException|JClassAlreadyExistsException ex) {
            error("Failed to generate GeneratedWebSite!", processingEnv);
            error(ExceptionUtils.getStackTrace(ex), processingEnv);
            return false;
        }

    }

    private void createClass() throws JClassAlreadyExistsException {
        generatedWebSiteClass = codeModel._class(JMod.PUBLIC | JMod.ABSTRACT, "com.github.webdriverextensions.generator.GeneratedWebSite", ClassType.CLASS);
        generatedWebSiteClass._extends(codeModel.ref(WebSite.class));
    }

    private void createFields() {
        // Declare WebPages
        for (TypeElement webPageElement : webPageElements) {
            JClass webPageClass = codeModel.ref(webPageElement.getQualifiedName().toString());
            generatedWebSiteClass.field(JMod.PUBLIC, webPageClass, getFieldName(webPageElement));
        }

        // Declare WebRepositories
        for (TypeElement webRepositoryElement : webRepositoryElements) {
            JClass webRepositoryClass = codeModel.ref(webRepositoryElement.getQualifiedName().toString());
            generatedWebSiteClass.field(JMod.PUBLIC, webRepositoryClass, getFieldName(webRepositoryElement));
        }

        // Declare Other
        for (TypeElement otherElement : otherElements) {
            JClass otherClass = codeModel.ref(otherElement.getQualifiedName().toString());
            JFieldVar field = generatedWebSiteClass.field(JMod.PUBLIC, otherClass, getFieldName(otherElement));
            if (hasFindBysAnnotation(otherElement)) {
                annotateFieldWithFindBysAnnotation(field, getFindBysAnnotation(otherElement));
            }
            if (hasFindByAnnotation(otherElement)) {
                annotateFieldWithFindByAnnotation(field, getFindByAnnotation(otherElement));
            }
        }
    }

    private void generate() throws IOException {
        CodeWriter codeWriter = new ProcessingEnvironmentCodeWriter(processingEnv);
        codeModel.build(codeWriter);
    }
}
