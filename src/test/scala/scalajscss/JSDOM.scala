package scalajscss

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("jsdom", "JSDOM")
class JSDOM(html: String) extends js.Object {

  val window: js.Dynamic = js.native
}
