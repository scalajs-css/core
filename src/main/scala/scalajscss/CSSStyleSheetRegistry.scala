package scalajscss

import scala.scalajs.{LinkingInfo, js}
import org.scalajs.dom
import org.scalajs.dom.ext.PimpedNodeList
import org.scalajs.dom.raw.HTMLStyleElement

import scala.collection.mutable.Map
import scala.scalajs.js.JSON

object CSSStyleSheetRegistry {

  private var plugins: Seq[ScalaJSCSSPlugin] = null

  private var order: js.Dictionary[Int] = null

  private var attachedOrder: js.Dictionary[Int] = null

  /**x
    * Provide order in which sheets should be attached to head ,
    * useful when you're creating a reusable library
    * @param sheets
    */
  def setOrder(sheets: CSSStyleSheet*) = {
    order = js.Dictionary()
    sheets.zipWithIndex.foreach {
      case (s, i) => {
        order(s.name) = i
      }
    }
  }

  /**
    * plugins to process your css (example: AutoPrefixer)
    * @param p
    */
  def setPlugins(p: ScalaJSCSSPlugin*) = {
    plugins = p
  }

  /**
    * use this method attach your multiple style sheets to head and to clear css string from memory.
    * @param sheets
    */
  def addToDocument(sheets: CSSStyleSheet*) = {
    sheets.foreach(s => registerAndAttach(s, true))
  }

  /**
    * use this method attach your single sheet to head and to clear css string from memory.
    * @param sheet
    */
  def addToDocument(sheet: CSSStyleSheet) = {
    registerAndAttach(sheet, true)
  }

  /**
    * it applies all plugins to rawCSS and then attaches to DOM
    * @param sheet
    * @param clearFromMemory
    */
  private def registerAndAttach(sheet: CSSStyleSheet,
                                clearFromMemory: Boolean = false) = {
    if (!sheet.registered) {
      if (plugins != null) {
        plugins.foreach(p => sheet._IAM_A_BAD_GUY = p.process(sheet))
      }
      sheet.registered = true
    }
    if (!sheet.attached) {
      val elm =
        dom.document.createElement("style").asInstanceOf[HTMLStyleElement]
      elm.`type` = "text/css"
      elm.setAttribute("id", sheet.name)
      elm.setAttribute("data-scalajscss", "")
      if (sheet.media != null) elm.setAttribute("media", sheet.media)
      val textNode = dom.document.createTextNode(sheet._IAM_A_BAD_GUY)
      elm.appendChild(textNode)
      if (order == null) {
        dom.document.head.appendChild(elm)
      } else {
        val index = order.find(_._1 == sheet.name).map(_._2).getOrElse(-1)
        if (index == -1) dom.document.head.appendChild(elm)
        else if (attachedOrder == null) {
          attachedOrder = js.Dictionary()
          dom.document.head.appendChild(elm)
          attachedOrder(sheet.name) = index
        } else {
          val lowerBound = attachedOrder.filter(_._2 < index)
          if (lowerBound.isEmpty) {
            val higherBound = attachedOrder.filter(_._2 > index).minBy(_._2)
            val referenceNode =
              dom.document.getElementById(higherBound._1)

            referenceNode.parentNode.insertBefore(elm, referenceNode)
          } else {
            val referenceNode =
              dom.document.getElementById(lowerBound.maxBy(_._2)._1)
            referenceNode.parentNode
              .insertBefore(elm, referenceNode.nextElementSibling)
          }
          attachedOrder(sheet.name) = index
        }
      }
      sheet.attached = true
    }
    if (clearFromMemory) sheet._IAM_A_BAD_GUY = ""
  }

  /**
    * use this method remove style sheet from DOM
    * @param sheets
    */
  def removeFromDocument(sheets: CSSStyleSheet*) = {
    sheets.foreach(sheet => {
      if (sheet.attached) {
        if (LinkingInfo.developmentMode) {
          if (sheet._IAM_A_BAD_GUY == "") {
            dom.window.console.warn(
              s"You're are trying to remove a sheet : ${sheet.name} which doesn't have css in memory,you can't attach it back after this step.Ignore this message if it's intentional!,if not use `addToDocumentAndKeepCSSInMemory` instead of `addToDocument` while adding to document.")
          }
        }
        dom.document.head.removeChild(dom.document.getElementById(sheet.name))
      } else {
        if (LinkingInfo.developmentMode) {
          dom.window.console.warn(
            s"You're are trying to remove a sheet : ${sheet.name} which is not attached to document!.")
        }
      }
    })
  }

  /**
    *  Use this method when you're attaching multiple sheets and want to remove later and add it again
    * @param sheets
    */
  def addToDocumentAndKeepCSSInMemory(sheets: CSSStyleSheet*) = {
    sheets.foreach(s => registerAndAttach(s))
  }

  /**
    *  Use this method when you're attaching single sheet and want to remove later and add it again
    * @param sheet
    */
  def addToDocumentAndKeepCSSInMemory(sheet: CSSStyleSheet) = {
    registerAndAttach(sheet)
  }
}
