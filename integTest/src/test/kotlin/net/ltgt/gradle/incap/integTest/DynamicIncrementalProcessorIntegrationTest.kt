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
package net.ltgt.gradle.incap.integTest

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DynamicIncrementalProcessorIntegrationTest {
    @JvmField
    @Rule
    val testProjectDir = TemporaryFolder()

    private val version = System.getProperty("version")!!

    @Test
    fun testDynamicProcessor() {
        // given
        setupProject()
        with(compileJava()) {
            // compile once
            assertThat(output).contains("Full recompilation is required because no incremental change information is available.")
        }

        // when
        val sourceFile = testProjectDir.newFile("src/main/java/test/AnotherAnnotatedClass.java").apply {
            writeText(
                """
                package test;

                @TestAnnotation
                class AnotherAnnotatedClass {}
                """.trimIndent()
            )
        }
        with(compileJava()) {
            // then
            assertThat(output).doesNotContainMatch("Full recompilation is required ")
            assertThat(output).contains("src/main/java/test/AnotherAnnotatedClass.java has been added")
            assertThat(output).doesNotContain("src/main/java/test/AnnotatedClass.java")
            assertThat(output).contains("Incremental compilation of 1 classes completed")
        }

        // when
        sourceFile.writeText(
            """
            package test;

            @TestAnnotation
            class AnotherAnnotatedClass {
                void foo() {}
            }
            """.trimIndent()
        )
        with(compileJava()) {
            // then
            assertThat(output).doesNotContainMatch("Full recompilation is required because .* is not incremental")
            assertThat(output).contains("src/main/java/test/AnotherAnnotatedClass.java has changed")
            assertThat(output).doesNotContain("src/main/java/test/AnnotatedClass.java")
            assertThat(output).contains("Incremental compilation of 2 classes completed")
        }
    }

    private fun setupProject() {
        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            include(":processor")
            """.trimIndent()
        )

        val testRepository = File("build/repository").absolutePath.replace(File.separatorChar, '/')
        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            plugins {
                `java-library`
            }
            repositories {
                maven { url = uri("$testRepository") }
            }
            dependencies {
                annotationProcessor(project(":processor"))
            }
            """.trimIndent()
        )
        testProjectDir.newFolder("src", "main", "java", "test")
        testProjectDir.newFile("src/main/java/test/TestAnnotation.java").writeText(
            """
            package test;

            import java.lang.annotation.ElementType;
            import java.lang.annotation.Retention;
            import java.lang.annotation.RetentionPolicy;
            import java.lang.annotation.Target;

            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.SOURCE)
            public @interface TestAnnotation {}
            """.trimIndent()
        )
        testProjectDir.newFile("src/main/java/test/AnnotatedClass.java").writeText(
            """
            package test;

            @TestAnnotation
            class AnnotatedClass {}
            """.trimIndent()
        )
        testProjectDir.newFolder("processor")
        testProjectDir.newFile("processor/build.gradle.kts").writeText(
            """
            plugins {
                `java-library`
            }
            repositories {
                maven { url = uri("$testRepository") }
            }
            dependencies {
                implementation("net.ltgt.gradle.incap:incap:$version")
                annotationProcessor("net.ltgt.gradle.incap:incap-processor:$version")
            }
            """.trimIndent()
        )
        testProjectDir.newFolder("processor", "src", "main", "java", "test", "processor")
        testProjectDir.newFile("processor/src/main/java/test/processor/TestAnnotationProcessor.java").writeText(
            """
            package test.processor;

            import java.io.IOException;
            import java.io.Writer;
            import java.util.Collections;
            import java.util.Set;
            import javax.annotation.processing.AbstractProcessor;
            import javax.annotation.processing.RoundEnvironment;
            import javax.annotation.processing.SupportedAnnotationTypes;
            import javax.lang.model.element.TypeElement;
            import javax.lang.model.util.ElementFilter;
            import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
            import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

            @SupportedAnnotationTypes("test.TestAnnotation")
            @IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.DYNAMIC)
            public class TestAnnotationProcessor extends AbstractProcessor {
              @Override
              public Set<String> getSupportedOptions() {
                return Collections.singleton(IncrementalAnnotationProcessorType.ISOLATING.getProcessorOption());
              }

              @Override
              public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                TypeElement testAnnotation = processingEnv.getElementUtils().getTypeElement("test.TestAnnotation");
                ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(testAnnotation)).forEach(e -> {

                  try (Writer w = processingEnv.getFiler().createSourceFile(e.getQualifiedName() + "_Generated", e).openWriter()) {
                    w.write(
                        "package " + processingEnv.getElementUtils().getPackageOf(e).getQualifiedName() + ";\n" +
                        "\n" +
                        "class " + e.getSimpleName() + "_Generated {}"
                    );
                  } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                  }
                });
                return false;
              }
            }
            """.trimIndent()
        )
        testProjectDir.newFolder("processor", "src", "main", "resources", "META-INF", "services")
        testProjectDir.newFile("processor/src/main/resources/META-INF/services/javax.annotation.processing.Processor")
            .writeText("test.processor.TestAnnotationProcessor")
    }

    private fun compileJava() = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments("--info", "compileJava")
        .build()
}
