package scalajscss
import org.scalajs.dom
import org.scalajs.dom.ext.PimpedHtmlCollection

import scala.language.experimental.macros
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.Random

class ScalaJSCSSTest extends BaseTest {

  object styles extends CSSStyleSheet {

    import dsl._
    import units._

    val container =
      style(backgroundColor := "red",
            display.flex,
            width := 100.%%,
            zIndex := 100,
            textUnderlinePosition.`under left`,
            transformStyle.preserve3d)

    val container2 = style(display.flex, flexDirection.columnReverse)

    val container4 =
      style(backgroundColor := "blue")

    // prefix with other class names
    val container3 =
      styleExtend(container)(color.red)

    // style suffix (pseudo classes , pseudo elements , random child element)
    styleSuffix(container, PseudoClasses.nthChild(46))(
      backgroundColor := "yellow")

    styleSuffix(container, PseudoClasses.hover)(backgroundColor := "red")

    styleSuffix(container, PseudoElements.after)(
      content := "\"Look at this orange box.\"",
      backgroundColor := "orange")

    styleSuffix(container2, " " + Tags.li)(color.green)

    //media queries
    media("(max-width: 1024px)")(
      styleVariant(container)(backgroundColor := "yellow"))

    // key frames
    keyframes("identifier1")(
      keyframeBlock("from")(top := "0"),
      keyframeBlock("to")(top := 100.px)
    )

    //global styles
    styleGlobal(Tags.body)(backgroundColor := "white")
    styleGlobal(Tags.a)(color := "purple")

  }

  object styles01 extends CSSStyleSheet {
    import dsl._
    val container = style(display.flex)
  }
  object styles02 extends CSSStyleSheet {
    import dsl._
    val container = style(display.flex)
  }
  object styles03 extends CSSStyleSheet {
    import dsl._
    val container = style(display.flex)
  }
  object styles04 extends CSSStyleSheet {
    import dsl._
    val container = style(display.flex)
  }
  object styles05 extends CSSStyleSheet {
    import dsl._
    val container = style(display.flex)
  }

  test(
    "Test StyleSheet",
    () => {

      val expected =
        s"""
         |.scalajscss-ScalaJSCSSTest-styles-container {
         |background-color: red;
         |display: flex;
         |width: 100%;
         |z-index: 100;
         |text-underline-position: under left;
         |transform-style: preserve-3d;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container2 {
         |display: flex;
         |flex-direction: column-reverse;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container4 {
         |background-color: blue;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container3 {
         |color: red;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container:nth-child(46) {
         |background-color: yellow;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container:hover {
         |background-color: red;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container::after {
         |content: "Look at this orange box.";
         |background-color: orange;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container2 li {
         |color: green;
         |}
         |@media (max-width: 1024px) {
         |.scalajscss-ScalaJSCSSTest-styles-container {
         |background-color: yellow;
         |}
         |}
         |@keyframes identifier1 {
         |from {
         |top: 0;
         |}
         |to {
         |top: 100px;
         |}
         |}
         |body {
         |background-color: white;
         |}
         |a {
         |color: purple;
         |}
     """.stripMargin

      println(s"raw css ${styles.css}")
      assert(styles.css == expected.substring(
        0,
        expected.lastIndexOf("}") + 1)) // TODO check this later to handle extra spaces added by stripmargin
    }
  )

  test(
    "Plugins",
    () => {

      val specialFlexExpected =
        s"""
         |.scalajscss-ScalaJSCSSTest-styles-container {
         |background-color: red;
         |display: special-flex;
         |width: 100%;
         |z-index: 100;
         |text-underline-position: under left;
         |transform-style: preserve-3d;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container2 {
         |display: special-flex;
         |flex-direction: column-reverse;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container4 {
         |background-color: blue;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container3 {
         |color: red;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container:nth-child(46) {
         |background-color: yellow;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container:hover {
         |background-color: red;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container::after {
         |content: "Look at this orange box.";
         |background-color: orange;
         |}
         |.scalajscss-ScalaJSCSSTest-styles-container2 li {
         |color: green;
         |}
         |@media (max-width: 1024px) {
         |.scalajscss-ScalaJSCSSTest-styles-container {
         |background-color: yellow;
         |}
         |}
         |@keyframes identifier1 {
         |from {
         |top: 0;
         |}
         |to {
         |top: 100px;
         |}
         |}
         |body {
         |background-color: white;
         |}
         |a {
         |color: purple;
         |}
     """.stripMargin

      class SpecialFlexPlugin extends ScalaJSCSSPlugin {
        override def process(sheet: CSSStyleSheet): String = {
          sheet.css.replace("display: flex", "display: special-flex")
        }
      }
      CSSStyleSheetRegistry.setPlugins(new SpecialFlexPlugin())

      CSSStyleSheetRegistry.addToDocumentAndKeepCSSInMemory(styles)

      assert(
        styles.css == specialFlexExpected
          .substring(0, specialFlexExpected.lastIndexOf("}") + 1))

      CSSStyleSheetRegistry.removeFromDocument(styles)
    }
  )

  test(
    "StyleSheet Order",
    () => {

      CSSStyleSheetRegistry.setOrder(styles01,
                                     styles02,
                                     styles03,
                                     styles04,
                                     styles05)

      CSSStyleSheetRegistry.addToDocument(styles03)

      CSSStyleSheetRegistry.addToDocument(styles02)

      CSSStyleSheetRegistry.addToDocument(styles05)

      CSSStyleSheetRegistry.addToDocument(styles04, styles01)

      val list =
        dom.document.head.children.toList
          .filter(_.id.contains(styles01.name.init))

      assert(list.size == 5)
      assert(
        list
          .map(_.id)
          == List(styles01.name,
                  styles02.name,
                  styles03.name,
                  styles04.name,
                  styles05.name))

      CSSStyleSheetRegistry.removeFromDocument(styles01,
                                               styles02,
                                               styles03,
                                               styles04,
                                               styles05)
    }
  )

}
