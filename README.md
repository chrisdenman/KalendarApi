# [KalendarApi](https://github.com/chrisdenman/KalendarApi)

A Kotlin library for interacting with Apple's Calendar.

For usage information, please see the [docs](https://chrisdenman.github.io/KalendarApi/dokka/html/-kalendar-api/index.html).

## Integration  

Create an api instance using `uk.co.ceilingcat.kalendarapi.createKalendarApi()`. The implementation is typesafe and, the IDE should provide enough hints to make it simple to use.


## Implementation Details

-   Spawns `osascript` processes to execute: JavaScript & AppleScript scripts, to automate Calendar.
-   Uses the: Arrow-kt, and kotlinx-serialization, libraries.
-   Dependencies:
    - `io.arrow-kt:arrow-core:0.11.0`
    - `org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.20`
    - `org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1`
    - `@ExperimentalPathApi`
-   Languages:
    -   Kotlin
    -   JavaScript
    -   AppleScript


## System Requirements

-   JDK 11
-   OSX


## Todo

1.  Use the `IO` monad.
1.  Use a `StringMonoid` for interpolation.
1.  DSL for model creation.
1.  Calendar deletion.
1.  Timezone support.
1.  Is it possible to retrieve the uid for a calendar anymore so, operations can target unique entities? 
1.  Generate JavaScript with Kotlin.


## References

-  [Apple's Calendar Scripting Guide](https://developer.apple.com/library/archive/documentation/AppleApplications/Conceptual/CalendarScriptingGuide/Calendar-LocateanEvent.html#//apple_ref/doc/uid/TP40016646-CH95-SW7)

-  [Apple's JavaScript for Automation Release Notes](https://developer.apple.com/library/archive/releasenotes/InterapplicationCommunication/RN-JavaScriptForAutomation/Articles/OSX10-10.html#//apple_ref/doc/uid/TP40014508-CH109-SW1)

-  [Arrow-kt](https://arrow-kt.io)

-  [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)

-   [Dokka Gradle Plugin](https://kotlin.github.io/dokka/1.4.20/user_guide/gradle/usage/)


## Licensing

[The Unlicense](LICENSE)
