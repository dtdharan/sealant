plugins {
    id("dev.steinerok.sealant.android-library")
    id("dev.steinerok.sealant.spotless")
    id("dev.steinerok.sealant.publish-module")
}

android {
    namespace = "dev.steinerok.sealant.fragment"
}

dependencies {
    api(projects.sealant.coreApi)

    api(libs.androidx.fragment.mainKtx)
}
