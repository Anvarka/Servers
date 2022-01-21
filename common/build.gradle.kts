import com.google.protobuf.gradle.proto
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

apply {
    plugin("com.google.protobuf")
}

sourceSets {
    main {
        proto {
            srcDir("proto")
        }
    }
}

protobuf {
    generatedFilesBaseDir = "$projectDir/src/"
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.0"
    }
}
