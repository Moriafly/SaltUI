[![Maven Central](https://img.shields.io/maven-central/v/io.github.moriafly/salt-ui)](https://search.maven.org/search?q=g:io.github.moriafly) [![CodeFactor](https://www.codefactor.io/repository/github/moriafly/saltui/badge/main)](https://www.codefactor.io/repository/github/moriafly/saltui/overview/main)

# Salt UI

Salt UI is UI components based on [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform). The 1.0 version is derived from some UI components of [Salt Player](https://github.com/Moriafly/SaltPlayerSource). Currently, Salt UI is used in Salt Player, Emo Scroll, Qinalt and other App to serve hundreds of thousands of users.

## Compatibility

| Salt UI        | Compose Multiplatform | Compose |
|----------------|-----------------------|---------|
| 2.3.1+         | 1.7.3                 | 1.7.6   |
| 2.3.0-alpha02+ | 1.7.1                 | 1.7.5   |
| 2.2.0+         | 1.7.0                 | 1.7.1   |
| 2.2.0-beta01+  | 1.7.0-rc01            | 1.7.0   |
| 2.2.0-alpha01+ | 1.7.0-beta02          |         |
| 2.0.7+         | 1.7.0-alpha03         |         |
| 2.0.4+         | 1.7.0-alpha02         |         |
| < 2.0.4        | 1.6.11                |         |

## Get started

Add dependency:

```kotlin
// Replace <TAG> with the latest version
// e.g. implementation("io.github.moriafly:salt-ui:2.0.0")
implementation("io.github.moriafly:salt-ui:<TAG>")
```

Simple start:

```kotlin
@Composable
fun App() {
    SaltTheme(
        configs = saltConfigs()
    ) {
        // ...
    }
}
```

See demo: [composeApp](https://github.com/Moriafly/SaltUI/tree/main/composeApp).

## Text Description and Translation Standards

See: [Salt UI Text Description and Translation Standards](https://moriafly.com/standards/tdts).

## License

See [LICENSE](LICENSE).

## Contribute

See [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md).