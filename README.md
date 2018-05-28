# gradle-incap-helper

Helper library and annotation processor for building incremental annotation processors

[Gradle 4.7 comes](https://docs.gradle.org/4.7/release-notes.html) with some level incremental annotation processing support.
[Gradle 4.8 goes farther](https://docs.gradle.org/4.8-rc-2/release-notes.html#improved-incremental-annotation-processing) by making it possibly dynamic.

This library and annotation processor helps you generate the META-INF descriptor,
and return the appropriate value from your processor's `getSupportedOptions()` if it's dynamic.

## Acknowledgements

This processor works great with [`@AutoService`](https://github.com/google/auto/tree/master/service),
which also inspired some of the code here.

Shout-out [Gradle, Inc.](https://gradle.com/) (and [Groupon](https://engineering.groupon.com/)) who built this feature.
