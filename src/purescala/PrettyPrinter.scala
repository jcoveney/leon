package purescala


/** This pretty-printer uses Unicode for some operators, to make sure we
 * distinguish PureScala from "real" Scala (and also because it's cute). */
object PrettyPrinter {
  import Common._
  import Trees._
  import TypeTrees._
  import Definitions._

  import java.lang.StringBuffer

  def apply(tree: Expr): String = {
    val retSB = pp(tree, new StringBuffer)
    retSB.toString
  }

  def apply(tpe: TypeTree): String = {
    val retSB = pp(tpe, new StringBuffer)
    retSB.toString
  }

  def apply(defn: Definition): String = {
    val retSB = pp(defn, new StringBuffer, 0)
    retSB.toString
  }

  // EXPRESSIONS
  // all expressions are printed in-line
  private def ppUnary(sb: StringBuffer, expr: Expr, op: String): StringBuffer = {
    var nsb: StringBuffer = sb
    nsb.append(op)
    nsb.append("(")
    nsb = pp(expr, nsb)
    nsb.append(")")
    nsb
  }

  private def ppBinary(sb: StringBuffer, left: Expr, right: Expr, op: String): StringBuffer = {
    var nsb: StringBuffer = sb
    nsb.append("(")
    nsb = pp(left, nsb)
    nsb.append(op)
    nsb = pp(right, nsb)
    nsb.append(")")
    nsb
  }

  private def ppNary(sb: StringBuffer, exprs: Seq[Expr], op: String): StringBuffer = {
    var nsb = sb
    nsb.append("(")
    val sz = exprs.size
    var c = 0

    exprs.foreach(ex => {
      nsb = pp(ex, nsb) ; c += 1 ; if(c < sz) nsb.append(op)
    })

    nsb.append(")")
    nsb
  }

  private def pp(tree: Expr, sb: StringBuffer): StringBuffer = tree match {
    case Variable(id) => sb.append(id)
    case And(exprs) => ppNary(sb, exprs, " \u2227 ")            // \land
    case Or(exprs) => ppNary(sb, exprs, " \u2228 ")             // \lor
    case Not(Equals(l, r)) => ppBinary(sb, l, r, " \u2260 ")    // \neq
    case Not(expr) => ppUnary(sb, expr, "\u00AC")               // \neg
    case Equals(l,r) => ppBinary(sb, l, r, " == ")
    case IntLiteral(v) => sb.append(v)
    case BooleanLiteral(v) => sb.append(v)
    case Plus(l,r) => ppBinary(sb, l, r, " + ")
    case Minus(l,r) => ppBinary(sb, l, r, " - ")
    case Times(l,r) => ppBinary(sb, l, r, " * ")
    case Division(l,r) => ppBinary(sb, l, r, " / ")
    case LessThan(l,r) => ppBinary(sb, l, r, " < ")
    case GreaterThan(l,r) => ppBinary(sb, l, r, " > ")
    case LessEquals(l,r) => ppBinary(sb, l, r, " \u2264 ")      // \leq
    case GreaterEquals(l,r) => ppBinary(sb, l, r, " \u2265 ")   // \geq
    
    case IfExpr(c, t, e) => {
      var nsb = sb
      nsb.append("if (")
      nsb = pp(c, nsb)
      nsb.append(") { ")
      nsb.append(t)
      nsb.append(" } else { ")
      nsb.append(e)
      nsb.append(" }")
      nsb
    }

    case ResultVariable() => sb.append("#res")

    case _ => sb.append("Expr?")
  }

  // TYPE TREES
  // all type trees are printed in-line
  private def pp(tpe: TypeTree, sb: StringBuffer): StringBuffer = tpe match {
    case Int32Type => sb.append("Int")
    case BooleanType => sb.append("Boolean")
    case _ => sb.append("Type?")
  }

  // DEFINITIONS
  // all definitions are printed with an end-of-line
  private def pp(defn: Definition, sb: StringBuffer, lvl: Int): StringBuffer = {
    def ind(sb: StringBuffer, customLevel: Int = lvl) : Unit = {
        sb.append("  " * customLevel)
    }

    defn match {
      case Program(id, mainObj) => {
        assert(lvl == 0)
        sb.append("package ")
        sb.append(id)
        sb.append(" {\n")
        pp(mainObj, sb, lvl+1).append("}\n")
      }

      case ObjectDef(id, defs, invs) => {
        var nsb = sb
        ind(nsb)
        nsb.append("object ")
        nsb.append(id)
        nsb.append(" {\n")

        var c = 0
        val sz = defs.size

        defs.foreach(df => {
          nsb = pp(df, nsb, lvl+1)
          if(c < sz - 1) {
            nsb.append("\n")
          }
          c = c + 1
        })

        ind(nsb); nsb.append("}\n")
      }

      case FunDef(id, rt, args, body, pre, post) => {
        var nsb = sb

        pre.foreach(prec => {
          ind(nsb)
          nsb.append("@pre : ")
          nsb = pp(prec, nsb)
          nsb.append("\n")
        })

        post.foreach(postc => {
          ind(nsb)
          nsb.append("@post: ")
          nsb = pp(postc, nsb)
          nsb.append("\n")
        })

        ind(nsb)
        nsb.append("def ")
        nsb.append(id)
        nsb.append("(")

        val sz = args.size
        var c = 0
        
        args.foreach(arg => {
          nsb.append(arg.id)
          nsb.append(" : ")
          nsb = pp(arg.tpe, nsb)

          if(c < sz - 1) {
            nsb.append(", ")
          }
          c = c + 1
        })

        nsb.append(") : ")
        nsb = pp(rt, nsb)
        nsb.append(" = {\n")

        ind(nsb, lvl+1)
        nsb = pp(body, nsb)
        nsb.append("\n")

        ind(nsb)
        nsb.append("}\n")
      }

      case _ => sb.append("Defn?")
    }
  }
}