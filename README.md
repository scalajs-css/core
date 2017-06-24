# ScalaJS-CSS

Features : 

* Type Safe CSS
* Compile Time Generated 
* Built for Component Age 
* Extensible via Plugins

```scala
//add this to your build.sbt libraryDependencies
"scalajs-css" %%% "core" % "replaceThisWithLatestVersionNumberFromReleaseTags"
```


# Docs 

* [Define StyleSheet](# define stylesheet)
* [Add to Document](#add to document)
* [Remove from Document](#remove from document)
* [StyleSheet Order](#stylesheet order)
* [Plugins](#plugins)
* [ClassName Shrink](#classname shrink)
* [Community](#community)



### Define StyleSheet

```scala
  object styles extends CSSStyleSheet {

    import dsl._
    import scalajscss.units._

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
```

***Generated CSS*** 

```css
.packagename-styles-container {
background-color: red;
display: flex;
width: 100%;
z-index: 100;
text-underline-position: under left;
transform-style: preserve-3d;
}
.packagename-styles-container2 {
display: flex;
flex-direction: column-reverse;
}
.packagename-styles-container4 {
background-color: blue;
}
.scalajscss.ScalaJSCSSTest.styles.container3 {
color: red;
}
.packagename-styles-container:nth-child(46) {
background-color: yellow;
}
.packagename-styles-container:hover {
background-color: red;
}
.packagename-styles-container::after {
content: "Look at this orange box.";
background-color: orange;
}
.packagename-styles-container2 li {
color: green;
}
@media (max-width: 1024px) {
.packagename-styles-container {
background-color: yellow;
}
}
@keyframes identifier1 {
from {
top: 0;
}
to {
top: 100px;
}
}
body {
background-color: white;
}
a {
color: purple;
}
```


### Add to Document

```scala

// add single sheet 
CSSStyleSheetRegistry.addToDocument(styles)

// add multiple sheets
CSSStyleSheetRegistry.addToDocument(styles,styles2,style3)

```

***Note: Once StyleSheet(styles) added to document, adding it again and again has no effect***

### Remove from Document

```scala

// remove single sheet 
CSSStyleSheetRegistry.removeFromDocument(styles)

// remove multiple sheets
CSSStyleSheetRegistry.removeFromDocument(styles,styles2,style3)

```

***Note: If you want to add removed styleSheet later at some point, make sure you used CSSStyleSheetRegistry.addToDocumentAndKeepCSSInMemory(..) while adding***


### StyleSheet Order

Let say you're building re usable component library and you have no control over which component is going to be used by end user. but your library depends on order of styles added to document(example : Button should be registered after BaseButton ,etc)

```scala

CSSStyleSheetRegistry.setOrder(BaseButton.styles,Button.styles)

```


### Plugins 


Use plugins to enhance raw css (example : autoprefixer,.. etc)

```scala

CSSStyleSheetRegistry.setPlugins(AutoPrefixer(),..)

```

### ClassName Shrink

By default class names emitted with `packagename` prefix 

Example : 

```scala

package org.domain.components

object styles extends CSSStyleSheet {
 
  val container = style(display.flex)
}
 
}

println(s"ClassName : ${styles.container}")// prints ClassName: org-domain-components-styles-container

```

For example in react apps we put all styles inside components package and we can make sure that inside components package each className will be unique, and we can easily omit `org-domain-` from our class names, to achieve this set macro setting  via scalacOptions in build.sbt


```scala
scalacOptions ++= Seq(
   ....,
  "-Xmacro-settings:classShrink=components"
)

```


# Community

If you have any questions regarding scalajs-css ,open a thread [here]()



