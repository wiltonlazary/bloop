package bloop.runner
import com.martiansoftware.nailgun.NGContext
import scopt.OParser
import java.util.concurrent.ConcurrentHashMap
import java.lang.ref.WeakReference
import java.nio.file.Path
import java.net.URLClassLoader
import java.net.URL

class ResidentRunner
object ResidentRunner {
  def nailMain(ctx: NGContext): Unit = {
    val args = ctx.getArgs()
    val builder = OParser.builder[ResidentRunnerParams]
    val separator = java.io.File.pathSeparatorChar
    // bloop-runner invalidate --entry entry1/ --entry entry7.jar
    // bloop-runner run --main-class foo.bar.Main --layer entry1/:entry2/ --layer entry3/:entry4/:entry5/:entry6/ --layer entry7.jar:entry8.jar:entry9.jar
    builder
      .cmd("run")
      .children(
        builder
          .arg[String]("layer")
          .valueName(s"<entry1>${separator}<entry2>")
      )
    ???
  }

  case class Layer(entries: Array[URL]) {
    val id: String = entries.toList.toString.hashCode.toString
  }

  val activeEntries = new ConcurrentHashMap[Path, Layer]()
  val activeClassloaders = new ConcurrentHashMap[String, WeakReference[ClassLoader]]()

  def invalidate(entries: Array[URL]): Unit = {
    entries.foreach { entry =>
      Option(activeEntries.get(entry)) match {
        case Some(layer) => Option(activeClassloaders.get(layer.id)).foreach(ref => ref.get())
        case None => () // TODO: Log
      }
    }
  }

  def run(mainClassName: String, args: Array[String], layers: List[Layer]): Unit = {
    val root = ClassLoader.getSystemClassLoader()
    val applicationLoader = layers.foldRight(root) {
      case (layer, parent) =>
        val loader = new URLClassLoader(layer.entries, parent)
        activeClassloaders.putIfAbsent(layer.id, new WeakReference(loader))
        loader
    }

    val mainClazz = applicationLoader.loadClass(mainClassName)
    val mainMethod = mainClazz.getMethod("main", classOf[Array[String]])
    mainMethod.invoke(null, args)
    ()
  }
}
