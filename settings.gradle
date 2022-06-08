includeBuild ("../annotation-tools/annotation-file-utilities") {
    if (!file("../annotation-tools/annotation-file-utilities").exists()) {
        exec {
            executable("./.ci-build-without-test.sh")
        }
    }
}
