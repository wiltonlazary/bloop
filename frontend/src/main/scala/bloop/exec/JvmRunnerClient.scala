package bloop.exec

import bloop.io.AbsolutePath
import bloop.logging.Logger
import bloop.cli.CommonOptions
import bloop.data.JdkConfig

import monix.eval.Task

case class JvmRunnerArgs(
    mainClass: String,
    classpathLayers: Array[Array[AbsolutePath]],
    mainArgs: Array[String],
    jvmOptions: Array[String],
    cwd: AbsolutePath
)

object JvmRunnerClient {

  /**
   * Connects to a resident runner JVM and trigger a run execution with the
   * given arguments. The run execution will be executed as fast as possible
   * by the resident JVM, taking advantage of e.g. class loader caching.
   *
   * If the resident JVM is not running, it will start it. If the jvm options
   * don't match, the resident JVM will be stopped and a new one will be started.
   */
  def runMain(args: JvmRunnerArgs, logger: Logger): Task[Int] = {
    val jvmOptions = args.jvmOptions.map(_.stripPrefix("-J"))
    ???
  }

  def toClassPathLayers(
      fullClasspath: Array[AbsolutePath],
      resources: Set[AbsolutePath]
  ): Array[Array[AbsolutePath]] = {
    val projectsLayer = fullClasspath.takeWhile(x => x.isDirectory || resources.contains(x))
    val jarsLayer = fullClasspath.slice(projectsLayer.size, fullClasspath.size)
    fullClasspath.iterator.zipWithIndex.filter(_._1.isDirectory).toArray
    ???
  }
}
