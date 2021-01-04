/*
 * Copyright © 2018 Thomas Broyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ltgt.gradle.incap.processor;

import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

@AutoService(Processor.class)
public class IncrementalAnnotationProcessorProcessor extends AbstractProcessor {

  private static final String ANNOTATION_NAME =
      IncrementalAnnotationProcessor.class.getCanonicalName();
  private static final String ANNOTATION_SIMPLE_NAME =
      IncrementalAnnotationProcessor.class.getSimpleName();
  private static final String GET_SUPPORTED_OPTIONS = "getSupportedOptions";

  static final String RESOURCE_FILE = "META-INF/gradle/incremental.annotation.processors";

  private final TreeMap<String, IncrementalAnnotationProcessorType> processors = new TreeMap<>();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton(ANNOTATION_NAME);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    try {
      processImpl(roundEnv);
    } catch (Exception e) {
      // We don't allow exceptions of any kind to propagate to the compiler
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      fatalError(writer.toString());
    }
    return false;
  }

  private void processImpl(RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      generateConfigFiles();
    } else {
      processAnnotations(roundEnv);
    }
  }

  private void processAnnotations(RoundEnvironment roundEnv) {
    TypeElement processor =
        processingEnv.getElementUtils().getTypeElement(Processor.class.getCanonicalName());
    TypeElement abstractProcessor =
        processingEnv.getElementUtils().getTypeElement(AbstractProcessor.class.getCanonicalName());
    for (Element e : roundEnv.getElementsAnnotatedWith(IncrementalAnnotationProcessor.class)) {
      if (!checkAnnotatedElement(e, processor)) {
        continue;
      }
      IncrementalAnnotationProcessorType processorType =
          e.getAnnotation(IncrementalAnnotationProcessor.class).value();
      if (processorType == IncrementalAnnotationProcessorType.DYNAMIC
          && processingEnv.getTypeUtils().isSubtype(e.asType(), abstractProcessor.asType())) {
        Element getSupportedOptions =
            processingEnv.getElementUtils().getAllMembers((TypeElement) e).stream()
                .filter(
                    method ->
                        method.getKind() == ElementKind.METHOD
                            && method.getSimpleName().contentEquals(GET_SUPPORTED_OPTIONS)
                            && ((ExecutableElement) method).getParameters().isEmpty())
                .findFirst()
                .orElseThrow(AssertionError::new);
        if (abstractProcessor.equals(getSupportedOptions.getEnclosingElement())) {
          warning(
              "Dynamic incremental annotation processor should override "
                  + GET_SUPPORTED_OPTIONS
                  + "()",
              e);
        }
      }
      processors.put(
          processingEnv.getElementUtils().getBinaryName((TypeElement) e).toString(), processorType);
    }
  }

  private void generateConfigFiles() {
    Filer filer = processingEnv.getFiler();
    try {
      // TODO: merge with an existing file (in case of incremental compilation, in a
      // non-incremental-compile-aware environment; e.g. Maven)
      FileObject fileObject =
          filer.createResource(StandardLocation.CLASS_OUTPUT, "", RESOURCE_FILE);
      try (PrintWriter out =
          new PrintWriter(
              new OutputStreamWriter(fileObject.openOutputStream(), StandardCharsets.UTF_8))) {
        processors.forEach((processor, type) -> out.println(processor + "," + type.name()));
        if (out.checkError()) {
          throw new IOException("Error writing to the file");
        }
      }
    } catch (IOException e) {
      fatalError("Unable to create " + RESOURCE_FILE + ", " + e);
    }
  }

  private boolean checkAnnotatedElement(Element e, TypeElement processor) {
    switch (e.getKind()) {
      case CLASS:
        break;
      case ENUM:
      case INTERFACE:
      case ANNOTATION_TYPE:
        error("@" + ANNOTATION_SIMPLE_NAME + " cannot be applied to an " + e.getKind(), e);
        return false;
      default:
        // Let JavaC emit the error when checking the @Target(TYPE)
        return false;
    }

    if (!processingEnv.getTypeUtils().isSubtype(e.asType(), processor.asType())) {
      error(
          "@"
              + ANNOTATION_SIMPLE_NAME
              + " annotated class must implement "
              + processor.getQualifiedName(),
          e);
      return false;
    }

    if (e.getModifiers().contains(Modifier.ABSTRACT)) {
      error("@" + ANNOTATION_SIMPLE_NAME + " annotated class must not be abstract", e);
    }

    return true;
  }

  private void error(String msg, Element element) {
    message(Kind.ERROR, msg, element);
  }

  private void warning(String msg, Element element) {
    message(Kind.WARNING, msg, element);
  }

  private void message(Kind kind, String msg, Element element) {
    processingEnv.getMessager().printMessage(kind, msg, element, getAnnotationMirror(element));
  }

  private AnnotationMirror getAnnotationMirror(Element element) {
    return element.getAnnotationMirrors().stream()
        .filter(
            annotationMirror ->
                ((TypeElement) annotationMirror.getAnnotationType().asElement())
                    .getQualifiedName()
                    .contentEquals(ANNOTATION_NAME))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }

  private void fatalError(String msg) {
    processingEnv.getMessager().printMessage(Kind.ERROR, "FATAL ERROR: " + msg);
  }
}
