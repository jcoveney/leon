/* Copyright 2009-2014 EPFL, Lausanne */

package leon
package synthesis

import purescala.Common._
import purescala.Definitions._
import purescala.Constructors._
import purescala.Trees._
import purescala.TreeOps._
import purescala.DefOps._
import Witnesses._

case class ChooseInfo(fd: FunDef,
                      pc: Expr,
                      source: Expr,
                      ch: Choose) {

  val problem = Problem.fromChoose(ch, pc)
}

object ChooseInfo {
  def extractFromProgram(prog: Program): List[ChooseInfo] = {

    // Look for choose()
    val results = for (f <- prog.definedFunctions if f.body.isDefined;
                       ci <- extractFromFunction(prog, f)) yield {
      ci
    }

    results.sortBy(_.source.getPos)
  }

  def extractFromFunction(prog: Program, fd: FunDef): Seq[ChooseInfo] = {

    val actualBody = and(fd.precondition.getOrElse(BooleanLiteral(true)), fd.body.get)
    val term = Terminating(fd.typedWithDef, fd.params.map(_.id.toVariable))

    for ((ch, path) <- new ChooseCollectorWithPaths().traverse(actualBody)) yield {
      ChooseInfo(fd, and(path, term), ch, ch)
    }
  }
}
