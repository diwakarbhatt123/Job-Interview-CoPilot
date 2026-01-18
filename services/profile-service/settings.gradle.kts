rootProject.name = "profile-service"

includeBuild("../../shared/libs/auth-core")
include(":profile-service-client")

project(":profile-service-client").projectDir = file("client")
