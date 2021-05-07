
resolvers += Resolver.url("sbt-plugin-releases on bintray",new URL("https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.4")
addSbtPlugin("com.eed3si9n" %% "sbt-assembly" % "0.15.0")
