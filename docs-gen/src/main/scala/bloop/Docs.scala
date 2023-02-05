package bloop

import bloop.docs.{ReleasesModifier, LauncherReleasesModifier, Sonatype}
import mdoc.MainSettings

import scala.meta.io.AbsolutePath

object Docs {
  def main(args: Array[String]): Unit = {
    val cwd0 = AbsolutePath.workingDirectory
    // Depending on who runs it (sbt vs bloop), the current working directory is different
    val cwd = if (!cwd0.resolve("docs").isDirectory) cwd0.toNIO.getParent else cwd0.toNIO

    val settings = MainSettings()
      .withSiteVariables(
        Map(
          "VERSION" -> Sonatype.releaseBloop.version,
          "LATEST_VERSION" -> bloop.internal.build.BuildInfo.version,
          "BLOOP_MAVEN_VERSION" -> Sonatype.releaseBloopMaven.version,
          "BLOOP_GRADLE_VERSION" -> Sonatype.releaseBloopGradle.version
        )
      )
      .withArgs(args.toList)
      // it should work with mdoc when run inside bloop but it doesn't, let's wait until it's fixed
      .withIn(cwd.resolve("docs"))
      .withOut(cwd.resolve("out"))
      .withStringModifiers(
        List(
          new ReleasesModifier,
          new LauncherReleasesModifier
        )
      )

    val exitCode = _root_.mdoc.Main.process(settings)
    if (exitCode != 0) sys.exit(exitCode)
  }
}
