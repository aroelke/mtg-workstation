package editor.util

import java.util.StringJoiner
import scala.jdk.CollectionConverters._

/**
 * Deprecated collection methods that have been/will be replaced with Scala code.
 * @author Alec Roelke
 */
@deprecated object CollectionUtils {
  @deprecated def convertToList[E](obj: AnyRef, clazz: Class[E]) = {
    obj match {
      case l: java.util.List[?] => l.asScala.collect{ case e if clazz.isInstance(e) => clazz.cast(e) }.asJava
      case _ => throw IllegalArgumentException(s"expected list, got ${obj.getClass}")
    }
  }

  @deprecated def convertToSet[E](obj: AnyRef, clazz: Class[E]) = {
    obj match {
      case s: java.util.Set[?] => s.asScala.collect{ case e if clazz.isInstance(e) => clazz.cast(e) }.asJava
      case _ => throw IllegalArgumentException(s"expected set, got ${obj.getClass}")
    }
  }

  @deprecated def join(join: StringJoiner, objects: java.util.Collection[?]) = {
    objects.asScala.map(String.valueOf).foreach(join.add)
    join.toString
  }
}
