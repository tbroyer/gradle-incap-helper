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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Properties

class IncrementalAnnotationProcessorProcessorIntegrationTest {
    @JvmField
    @Rule
    val testProjectDir = TemporaryFolder()

    private val testJavaHome = System.getProperty("test.java-home", System.getProperty("java.home"))

    private val version = System.getProperty("version")!!
    private val testRepositories =
        System.getProperty("testRepositories")!!.splitToSequence(File.pathSeparator).joinToString("\n") {
            """
            maven { url = uri("${File(it).toURI().toASCIIString()}") }
            """.trimIndent()
        }

    @Test fun testIncrementality() {
        // given
        testProjectDir.newFile("gradle.properties").outputStream().use {
            Properties().apply {
                setProperty("org.gradle.java.home", testJavaHome)
                store(it, null)
            }
        }
        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            dependencyResolutionManagement {
                repositories {
                    ${testRepositories.prependIndent("    ".repeat(2))}
                }
            }
            """.trimIndent(),
        )

        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            plugins {
                `java-library`
            }
            dependencies {
                implementation("net.ltgt.gradle.incap:incap:$version")
                annotationProcessor("net.ltgt.gradle.incap:incap-processor:$version")
            }
            """.trimIndent(),
        )

        writeProcessor("SomeProcessor", "AGGREGATING", "test")
        writeProcessor("AnotherProcessor", "ISOLATING", "test")

        // when
        with(compileJava()) {
            // compile once
            assertThat(output).contains("Full recompilation is required because no incremental change information is available.")
        }

        // then
        assertTrue(generatedResourceFile.exists())
        assertEquals(
            "test.AnotherProcessor,ISOLATING\ntest.SomeProcessor,AGGREGATING",
            generatedResourceFile.readText().trim(),
        )

        // given
        writeProcessor("SomeProcessor", "DYNAMIC", "test")

        // when
        with(compileJava()) {
            // then
            assertThat(output).doesNotContain("Full recompilation is required ")
            assertThat(output).contains("Incremental compilation of 1 classes completed")
        }

        // then
        assertEquals(
            "test.AnotherProcessor,ISOLATING\ntest.SomeProcessor,DYNAMIC",
            generatedResourceFile.readText().trim(),
        )
    }

    private fun writeProcessor(
        className: String,
        processorType: String,
        vararg packageNames: String,
    ): Unit =
        File(
            testProjectDir.root,
            packageNames.joinToString(
                separator = "/",
                prefix = "src/main/java/",
                postfix = "/$className.java",
            ),
        ).run {
            parentFile.mkdirs()
            writeText(
                """
                package ${packageNames.joinToString(separator = ".")};
                import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.*;

                import java.util.Set;
                import javax.annotation.processing.AbstractProcessor;
                import javax.annotation.processing.RoundEnvironment;
                import javax.lang.model.element.TypeElement;
                import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

                @IncrementalAnnotationProcessor($processorType)
                public class $className extends AbstractProcessor {
                  @Override
                  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                    return false;
                  }
                }
                """.trimIndent(),
            )
        }

    private fun compileJava() =
        GradleRunner
            .create()
            .withProjectDir(testProjectDir.root)
            .withArguments("--info", "compileJava")
            .build()

    private val generatedResourceFile
        get() = testProjectDir.root.resolve("build/classes/java/main/META-INF/gradle/incremental.annotation.processors")
}
