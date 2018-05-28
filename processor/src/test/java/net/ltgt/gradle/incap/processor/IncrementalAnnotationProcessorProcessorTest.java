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

import static com.google.testing.compile.JavaSourcesSubject.assertThat;

import com.google.testing.compile.JavaFileObjects;
import java.nio.charset.StandardCharsets;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import org.junit.Test;

public class IncrementalAnnotationProcessorProcessorTest {
  @Test
  public void incrementalAnnotationProcessor() {
    assertThat(
            JavaFileObjects.forResource("test/IsolatingProcessor.java"),
            JavaFileObjects.forResource("test/AggregatingProcessor.java"),
            JavaFileObjects.forResource("test/DynamicProcessor.java"),
            JavaFileObjects.forResource("test/Enclosing.java"))
        .processedWith(new IncrementalAnnotationProcessorProcessor())
        .compilesWithoutError()
        .and()
        .generatesFileNamed(
            StandardLocation.CLASS_OUTPUT,
            "",
            IncrementalAnnotationProcessorProcessor.RESOURCE_FILE)
        .withStringContents(
            StandardCharsets.UTF_8,
            String.join(
                "\n",
                "test.AggregatingProcessor,AGGREGATING",
                "test.DynamicProcessor,DYNAMIC",
                "test.Enclosing$NestedIsolatingProcessor,ISOLATING",
                "test.IsolatingProcessor,ISOLATING",
                ""));
  }

  @Test
  public void annotatedInterface() {
    final JavaFileObject annotatedInterface =
        JavaFileObjects.forResource("test/bad/AnnotatedInterface.java");
    assertThat(annotatedInterface)
        .processedWith(new IncrementalAnnotationProcessorProcessor())
        .failsToCompile()
        .withErrorCount(1)
        .withErrorContaining("@" + IncrementalAnnotationProcessor.class.getSimpleName())
        .in(annotatedInterface)
        .onLine(22)
        .atColumn(1);
  }

  @Test
  public void annotatedEnum() {
    final JavaFileObject annotatedEnum = JavaFileObjects.forResource("test/bad/AnnotatedEnum.java");
    assertThat(annotatedEnum)
        .processedWith(new IncrementalAnnotationProcessorProcessor())
        .failsToCompile()
        .withErrorCount(1)
        .withErrorContaining("@" + IncrementalAnnotationProcessor.class.getSimpleName())
        .in(annotatedEnum)
        .onLine(21)
        .atColumn(1);
  }

  @Test
  public void annotatedAnnotation() {
    final JavaFileObject annotatedAnnotation =
        JavaFileObjects.forResource("test/bad/AnnotatedAnnotation.java");
    assertThat(annotatedAnnotation)
        .processedWith(new IncrementalAnnotationProcessorProcessor())
        .failsToCompile()
        .withErrorCount(1)
        .withErrorContaining("@" + IncrementalAnnotationProcessor.class.getSimpleName())
        .in(annotatedAnnotation)
        .onLine(21)
        .atColumn(1);
  }

  @Test
  public void notAProcessor() {
    final JavaFileObject notAProcessor = JavaFileObjects.forResource("test/bad/NotAProcessor.java");
    assertThat(notAProcessor)
        .processedWith(new IncrementalAnnotationProcessorProcessor())
        .failsToCompile()
        .withErrorCount(1)
        .withErrorContaining("@" + IncrementalAnnotationProcessor.class.getSimpleName())
        .in(notAProcessor)
        .onLine(21)
        .atColumn(1);
  }
}
