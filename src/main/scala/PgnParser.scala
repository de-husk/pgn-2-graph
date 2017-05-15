package pgn

object PgnParser {

  sealed trait ParseError
  object ParseError {
    case object MissingMoves extends ParseError
    case object MissingResult extends ParseError
  }

  def parseMoves(pgn: String): Either[ParseError, List[Move]] = {
    getMoveLine(pgn) match {
      case Some(s) => Right(parseFromMoveLine(s.toLowerCase.trim))
      case None => Left(ParseError.MissingMoves)
    }
  }

  def parseResult(pgn: String): Either[ParseError, GameResult] = {
    getResultTag(pgn) match {
      case Some(r) =>
        val result =
          if (r contains "1/2-1/2") {
            Result.Draw
          } else if (r contains "0-1") {
            Result.BlackVictory
          } else if (r contains "1-0") {
            Result.WhiteVictory
          } else {
            Result.Ongoing
          }
        Right(result)
      case None => Left(ParseError.MissingResult)
    }
  }

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
              Turn.White
            ),
            Move(
              SAN(index + 1, bm),
              Turn.Black
            )
          )
          case (Some(wm), None) => List(
            Move(
              SAN(index + 1, wm),
              Turn.White
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
