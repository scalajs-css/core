package scalajscss

package object units {
  final implicit class CSSUnits(val input: Double) extends AnyVal {
    @inline def px = input + "px"
    @inline def mm = input + "mm"
    @inline def cm = input + "cm"
    @inline def in = input + "in"
    @inline def pt = input + "pt"
    @inline def pc = input + "pc"
    @inline def em = input + "em"
    @inline def ex = input + "ex"
    @inline def ch = input + "ch"
    @inline def rem = input + "rem"
    @inline def vw = input + "vw"
    @inline def vh = input + "vh"
    @inline def %% = input + "%"
  }
}
