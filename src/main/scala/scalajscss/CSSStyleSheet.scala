package scalajscss
import org.scalajs.dom.raw.HTMLStyleElement

import scala.language.experimental.macros
import scala.scalajs.js
import scala.scalajs.js.|

trait CSSStyleSheet {

  object dsl extends CSSProperties {}

  type DummyStyle = Unit

  private[scalajscss] var registered: Boolean = false

  private[scalajscss] var attached: Boolean = false

  var _IAM_A_BAD_GUY: String = ""

  private[scalajscss] def css = _IAM_A_BAD_GUY

  final val name: String =
    this.getClass.getName.replace(".", "-").replace("$", "-").init

  val media: String = null

  def style(props: CSSProp.type*): String =
    macro ScalaJSCSSMacro.styleMacroImpl

  def styleExtend(styles: String*)(props: CSSProp.type*): String =
    macro ScalaJSCSSMacro.styleExtendMacroImpl

  def styleVariant(style: String)(props: CSSProp.type*): StyleVariant =
    macro ScalaJSCSSMacro.styleVariantMacroImpl

  def keyframeBlock(blockSelector: String)(
      props: CSSProp.type*): KeyframeBlock =
    macro ScalaJSCSSMacro.keyframeBlockMacroImpl

  def media(condition: String)(variants: StyleVariant*): DummyStyle =
    macro ScalaJSCSSMacro.mediaMacroImpl

  def keyframes(name: String)(blocks: KeyframeBlock*): DummyStyle =
    macro ScalaJSCSSMacro.keyframesMacroImpl

  def styleSuffix(style: String, suffix: String)(
      props: CSSProp.type*): DummyStyle =
    macro ScalaJSCSSMacro.styleSuffixMacroImpl

  def styleGlobal(name: String)(props: CSSProp.type*): DummyStyle =
    macro ScalaJSCSSMacro.styleGlobalMacroImpl
}

@js.native
trait StyleVariant extends js.Object

@js.native
trait KeyframeBlock extends js.Object

private[scalajscss] trait ScalaJSCSSPlugin {
  def process(sheet: CSSStyleSheet): String
}
