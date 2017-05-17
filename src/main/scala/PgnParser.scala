package pgn

sealed trait ParseError
case object MissingMoves extends ParseError
case object MissingResult extends ParseError

object PgnParser {

  def parseMoves(pgn: String): Either[ParseError, List[Move]] =
    getMoveLine(pgn)
      .map(s => parseFromMoveLine(s.toLowerCase.trim))
      .toRight(MissingMoves)

  def parseResult(pgn: String): Either[ParseError, Result] =
    getResultTag(pgn)
      .map(Result(_))
      .toRight(MissingResult)

  private def getResultTag(pgn: String): Option[String] =
    """\[Result(.+)\]""".r.findFirstIn(pgn)

  private def getMoveLine(pgn: String): Option[String] =
    """\s+(1\..*)""".r.findFirstIn(pgn)

  private def parseFromMoveLine(moveLine: String): List[Move] = {
    val moveNumRegex = """([0-9]+)\."""
    val commentaryRegex = """[{](.*?)[}]"""

    moveLine.replaceAll(commentaryRegex, "")
      .split(moveNumRegex).toList
      .filter({ s => !s.isEmpty })
      .map({ turn: String => parseMovesForTurn(turn) })
      .zipWithIndex
      .flatMap { case ((whiteMove, blackMove), index) =>
        (whiteMove , blackMove) match {
          case (Some(wm), Some(bm)) => List(
            Move(
              SAN(index + 1, wm),
              White
            ),
            Move(
              SAN(index + 1, bm),
              Black
            )
          )
          case (Some(wm), None) => List(
            Move(
              SAN(index + 1, wm),
              White
            )
          )
          case _ => List()
        }
    }
  }

  private def parseMovesForTurn(turn:String): (Option[String], Option[String]) = {
    val castleRegex = """o\-o\-o|o\-o|0\-0\-0|0\-0"""
    val sanRegex = (castleRegex ++ """|[pqkrbna-h][qkrbna-h1-8x\=]{1,6}""").r
    val m = sanRegex.findAllIn(turn)

    val whiteMove = if (m.hasNext){
      Some(m.next)
    } else {
      None
    }

    val blackMove = if (m.hasNext) {
      Some(m.next)
    } else {
      None
    }

    (whiteMove, blackMove)
  }
}
