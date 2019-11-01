/**
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

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.TextUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class IncrementalAnnotationProcessorProcessorIntegrationTest {
    @JvmField
    @Rule
    val testProjectDir = TemporaryFolder()

    private val version = System.getProperty("version")!!

    @Test fun testIncrementality() {
        // given
        testProjectDir.newFile("settings.gradle.kts")

        val testRepository = TextUtil.normaliseFileSeparators(File("build/repository").absolutePath)
        testProjectDir.newFile("build.gradle.kts").writeText(
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

        writeProcessor("SomeProcessor", "AGGREGATING", "test")
        writeProcessor("AnotherProcessor", "ISOLATING", "test")

        // when
        compileJava()

        // then
        assertTrue(generatedResourceFile.exists())
        assertEquals(
            "test.AnotherProcessor,ISOLATING\ntest.SomeProcessor,AGGREGATING",
            generatedResourceFile.readText().trim()
        )

        // given
        writeProcessor("SomeProcessor", "DYNAMIC", "test")

        // when
        compileJava()

        // then
        assertEquals(
            "test.AnotherProcessor,ISOLATING\ntest.SomeProcessor,DYNAMIC",
            generatedResourceFile.readText().trim()
        )
    }

    private fun writeProcessor(className: String, processorType: String, vararg packageNames: String): Unit =
        File(
            testProjectDir.root,
            TextUtil.normaliseFileSeparators(
                packageNames.joinToString(
                    separator = "/",
                    prefix = "src/main/java/",
                    postfix = "/$className.java"
                )
            )
        ).run {
            parentFile.mkdirs()
            writeText(
                """package ${packageNames.joinToString(separator = ".")};
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
                """.trimIndent()
            )
        }

    private fun compileJava() = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments("compileJava")
        .build()

    private val generatedResourceFile
        get() = testProjectDir.root.resolve(TextUtil.normaliseFileSeparators("build/classes/java/main/META-INF/gradle/incremental.annotation.processors"))
}
