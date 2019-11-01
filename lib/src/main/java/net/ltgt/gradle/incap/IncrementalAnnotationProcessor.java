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
package net.ltgt.gradle.incap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the type of incremental annotation processing the annotated processor is capable of.
 *
 * <p>This annotation must be placed on a concrete class implementing {@link
 * javax.annotation.processing.Processor}. A processor described as {@linkplain
 * IncrementalAnnotationProcessorType#DYNAMIC dynamic} will have to implement {@link
 * javax.annotation.processing.Processor#getSupportedOptions getSupportedOptions} returning zero or
 * one of the {@linkplain IncrementalAnnotationProcessorType#getProcessorOption() predefined
 * constants} looked for by Gradle.
 *
 * <p>The annotation processor will generate the appropriate {@code
 * META-INF/gradle/incremental.annotation.processors} descriptor file describing all annotated
 * processors.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface IncrementalAnnotationProcessor {
  IncrementalAnnotationProcessorType value();
}
