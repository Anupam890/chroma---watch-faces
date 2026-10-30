pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "Chrona"
include(":shared", ":mobile", ":wear")
include(
    ":watchfaces:classic_aurora",
    ":watchfaces:mono_digital",
    ":watchfaces:pulse_sport",
    ":watchfaces:memories_photo",
    ":watchfaces:skyline_weather"
)
