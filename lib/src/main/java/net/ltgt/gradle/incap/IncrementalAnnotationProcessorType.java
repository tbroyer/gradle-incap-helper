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

import java.util.Locale;

/**
 * Describes the type of incremental annotation processing a processor is capable of.
 *
 * <p>See <a
 * href="https://docs.gradle.org/4.8-rc-2/userguide/java_plugin.html#sec:incremental_annotation_processing">the
 * Gradle documentation</a> for more information on each type.
 */
public enum IncrementalAnnotationProcessorType {
  ISOLATING(true),
  AGGREGATING(true),
  DYNAMIC(false);

  private final boolean hasProcessorOption;

  IncrementalAnnotationProcessorType(boolean hasProcessorOption) {
    this.hasProcessorOption = hasProcessorOption;
  }

  /**
   * Returns the specific value that Gradle looks for in {@link
   * javax.annotation.processing.Processor#getSupportedOptions() getSupportedOptions} for {@link
   * #DYNAMIC} incremental annotation processors.
   *
   * @throws UnsupportedOperationException if called on the {@link #DYNAMIC} constant.
   */
  public String getProcessorOption() {
    if (!hasProcessorOption) {
      throw new UnsupportedOperationException();
    }
    return "org.gradle.annotation.processing." + name().toLowerCase(Locale.ROOT);
  }
}
