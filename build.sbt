val Version = "2.13.10"

lazy val root = project
  .in(file("."))
  .settings(
    name         := "Vbot",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := Version,
    libraryDependencies ++= Seq(
      "com.bot4s"                     %% "telegram-core"                 % "5.6.1",
      "dev.zio"                       %% "zio"                           % "2.0.3",
      "dev.zio"                       %% "zio-interop-cats"              % "22.0.0.0",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.8.3",
      "dev.zio"                       %% "zio-concurrent"                % "2.0.3",
      "org.scalactic"                 %% "scalactic"                     % "3.2.14",
      "org.scalatest"                 %% "scalatest"                     % "3.2.14",
      "com.github.pureconfig"         %% "pureconfig"                    % "0.17.2"
    )
  )
