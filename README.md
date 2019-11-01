# gradle-incap-helper

Helper library and annotation processor for building incremental annotation processors

[Gradle 4.7 comes](https://docs.gradle.org/4.7/release-notes.html) with some level incremental annotation processing support.
[Gradle 4.8 goes farther](https://docs.gradle.org/4.8-rc-2/release-notes.html#improved-incremental-annotation-processing) by making it possibly dynamic.

This library and annotation processor helps you generate the META-INF descriptor,
and return the appropriate value from your processor's `getSupportedOptions()` if it's dynamic.

## Usage

1. Add the `incap` library to your compile-time dependencies, and the `incap-processor` to your annotation processor path:

   <details>
     <summary>with Gradle</summary>

   ```gradle
   dependencies {
       compileOnly("net.ltgt.gradle.incap:incap:${incap.version}")
       annotationProcessor("net.ltgt.gradle.incap:incap-processor:${incap.version}")
   }
   ```

   </details>

   <details>
     <summary>with Maven</summary>

   ```xml
   <dependencies>
       <dependency>
           <groupId>net.ltgt.gradle.incap</groupId>
           <artifactId>incap</artifactId>
           <version>${incap.version}</version>
           <scope>provided</scope>
           <optional>true</optional>
       </dependency>
   </dependencies>
   <build>
       <plugins>
           <plugin>
               <groupId>org.apache.maven.plugins</groupId>
               <artifactId>maven-compiler-plugin</artifactId>
               <configuration>
                   <annotationProcessorPaths>
                       <path>
                           <groupId>net.ltgt.gradle.incap</groupId>
                           <artifactId>incap-processor</artifactId>
                           <version>${incap.version}</version>
                       </path>
                   </annotationProcessorPaths>
               </configuration>
           </plugin>
       </plugins>
   </build>
   ```

   </details>
   
   Note: it's OK to use `compileOnly` in Gradle, or the `provided` scope in Maven,
   despite the annotation having class retention, because annotation processors aren't libraries that others compile against.

2. Annotate your annotation processor with `@IncrementalAnnotationProcessor`

   ```java
   @IncrementalAnnotationProcessor(ISOLATING)
   public class MyProcessor extends AbstractProcessor {
   ```

3. If the choice of incrementality support is dynamic (i.e. you used the `DYNAMIC` value above), use the `IncrementalAnnotationProcessorType` enumeration's `getProcessorOption()` from your `getSupportedOptions()` to get the appropriate constant.

   ```java
   @Override
   public Set<String> getSupportedOptions() {
       if (someCondition) {
           return Collections.singleton(ISOLATING.getProcessorOption());
       } else {
           return Collections.emptySet();
       }
   }
   ```

## Acknowledgements

This processor works great with [`@AutoService`](https://github.com/google/auto/tree/master/service),
which also inspired some of the code here.

Shout-out [Gradle, Inc.](https://gradle.com/) (and [Groupon](https://engineering.groupon.com/)) who built this feature.
