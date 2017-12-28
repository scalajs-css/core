package scalajscss

import java.io.{File, PrintWriter}

import scala.io.Source
import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.sys.process.Process

private[scalajscss] object ScalaJSCSSMacro {

  private var classNameShrink: String = null

  def styleMacroImpl(c: blackbox.Context)(props: c.Tree*): c.Tree = {
    import c.universe._
    val className = getClassName(c)

    val cssProps = getCSSFromProps(c)(props)
    q"""
        import scala.language.reflectiveCalls
        import scalajs.js.JSConverters._
        import scalajs.js
         ${c.prefix.tree}._IAM_A_BAD_GUY += "\n." + $className + " {\n" + $cssProps + "}"
         $className
    """
  }

  def styleExtendMacroImpl(c: blackbox.Context)(styles: c.Tree*)(
      props: c.Tree*): c.Tree = {
    import c.universe._
    val className = getClassName(c)

    val stylesPrefix = getCombinedStyles(c)(styles)
    val cssProps = getCSSFromProps(c)(props)

    q"""
      {
        import scala.language.reflectiveCalls
        import scalajs.js.JSConverters._
        import scalajs.js
        ${c.prefix.tree}._IAM_A_BAD_GUY += "\n." + $className  + " {\n" + $cssProps + "}"
         $className + $stylesPrefix
      }
    """
  }

  def styleSuffixMacroImpl(c: blackbox.Context)(style: c.Tree, suffix: c.Tree)(
      props: c.Tree*): c.Tree = {
    import c.universe._
    val className = q"${style}"
    val cssProps = getCSSFromProps(c)(props)

    q"""
      {
        import scala.language.reflectiveCalls
        import scalajs.js.JSConverters._
        import scalajs.js
        ${c.prefix.tree}._IAM_A_BAD_GUY += "\n." + $className + $suffix + " {\n" + $cssProps + "}"
      }
    """
  }

  def mediaMacroImpl(c: blackbox.Context)(condition: c.Tree)(
      variants: c.Tree*): c.Tree = {
    import c.universe._

    if (variants.isEmpty)
      c.abort(c.enclosingPosition, "You must provide at least one styleVariant")

    val styleVariants = variants.map(t => q"$t")

    q"""
      {
        import scala.language.reflectiveCalls
        import scalajs.js.JSConverters._
        import scalajs.js
        ${c.prefix.tree}._IAM_A_BAD_GUY += "\n@media " + $condition + " {" + ..$styleVariants + "\n}"
      }
    """
  }

  def keyframesMacroImpl(c: blackbox.Context)(name: c.Tree)(
      blocks: c.Tree*): c.Tree = {
    import c.universe._

    if (blocks.isEmpty)
      c.abort(c.enclosingPosition,
              "You must provide at least one keyframe block")
    val blocksFinalTerm = TermName(c.freshName())

    val keyframeBlocks = blocks.map(t => q"$blocksFinalTerm += $t")

    q"""
      {
        import scala.language.reflectiveCalls
        import scalajs.js.JSConverters._
        import scalajs.js
        var $blocksFinalTerm = ""
        ..$keyframeBlocks
        ${c.prefix.tree}._IAM_A_BAD_GUY += "\n@keyframes " + $name + " {" + $blocksFinalTerm + "\n}"
      }
    """
  }

  def styleVariantMacroImpl(c: blackbox.Context)(style: c.Tree)(
      props: c.Tree*): c.Tree = {
    import c.universe._

    val cssProps = getCSSFromProps(c)(props)

    q"""
      {
        import scala.language.reflectiveCalls
        import scalajs.js.JSConverters._
        import scalajs.js

        ("\n." + $style + " {\n" + $cssProps + "}").asInstanceOf[scalajscss.StyleVariant]

      }
    """
  }

  def keyframeBlockMacroImpl(c: blackbox.Context)(blockSelector: c.Tree)(
      props: c.Tree*): c.Tree = {
    import c.universe._

    val cssProps = getCSSFromProps(c)(props)
    val finalTerm = TermName(c.freshName())

    q"""
      {
        import scala.language.reflectiveCalls
        import scalajs.js.JSConverters._
        import scalajs.js

        val $finalTerm = "\n" + $blockSelector + " {\n" + $cssProps + "}"
        $finalTerm.asInstanceOf[scalajscss.KeyframeBlock]
      }
    """
  }

  def styleGlobalMacroImpl(c: blackbox.Context)(name: c.Tree)(
      props: c.Tree*): c.Tree = {
    import c.universe._

    val cssProps = getCSSFromProps(c)(props)
    q"""
      {
        import scala.language.reflectiveCalls
        import scalajs.js.JSConverters._
        import scalajs.js

       ${c.prefix.tree}._IAM_A_BAD_GUY += "\n" + $name + " {\n" + $cssProps + "}"
      }
    """
  }

  @inline
  def getCSSFromProps(c: blackbox.Context)(props: Seq[c.Tree]) = {
    import c.universe._
    if (props.isEmpty)
      c.abort(c.enclosingPosition, "You must provide at least one css prop")
    val finalTerm = TermName(c.freshName())
    val cssKeyValuePairs = props.map(prop => {
      val (propName, propValue) = prop match {
        case q"$propName := $propValue" => (propName, propValue)
        case q"$propName.${propValue}" => {
          val value = s"$propValue"
          val s =
            if (util.Try(value.toDouble).isSuccess) value
            else
              camelCaseToHyphen(
                value.replace("$minus", "-").replace("$u0020", " "))
          (propName, q"$s")
        }
        case _ =>
          c.abort(c.enclosingPosition,
                  s"Specified prop ${prop} is not supported.")
      }

      val name =
        if (propName.symbol.typeSignature <:< typeOf[AlienNameProp]) {
          camelCaseToHyphen(propName.symbol.asTerm.name.decodedName.toString)
        } else {
          propName.symbol.asTerm.name.decodedName.toString
        }

      q""" $finalTerm += ${name} + ": " + $propValue + ";\n" """

    })

    q"""{
         var $finalTerm = ""
         ..$cssKeyValuePairs
         $finalTerm
         }
      """
  }

  @inline
  def getCombinedStyles(c: blackbox.Context)(styles: Seq[c.Tree]) = {
    import c.universe._

    val finalTerm = TermName(c.freshName())
    val stylesFlattened = styles.map(style => {
      q""" $finalTerm += " " + ${style}  """

    })

    q"""{
         var $finalTerm = ""
         ..$stylesFlattened
         $finalTerm
         }
      """
  }

  @inline
  def getClassName(c: blackbox.Context): String = {
    val name = c.internal.enclosingOwner.fullName
    if (classNameShrink != null)
      if (name.contains(classNameShrink))
        name
          .substring(name
            .indexOf("." + classNameShrink + ".") + classNameShrink.length + 2)
          .replace(".", "-")
      else name.replace(".", "-")
    else {
      val CLASS_NAME_SHRINK_SETTING = "classNameShrink="
      val setting = c.settings
        .find(_.contains(CLASS_NAME_SHRINK_SETTING))
        .map(_.replace(CLASS_NAME_SHRINK_SETTING, "").trim)
        .getOrElse("")
      if (setting.isEmpty) name.replace(".", "-")
      else {
        classNameShrink = setting
        getClassName(c)
      }
    }

  }

  @inline
  def camelCaseToHyphen(name: String) =
    "[A-Z\\d]".r
      .replaceAllIn(name, { m =>
        "-" + m.group(0).toLowerCase()
      })

  @inline
  def writeToFile(path: String, content: String) = {
    val pw = new PrintWriter(new File(path))
    try pw.write(content)
    finally pw.close()
  }
}
