package pgn

case class Pgn(
  moves: List[Move],
  result: Result
)

object Pgn {
  def apply(pgn: String): Either[ParseError, Pgn] = {
    (PgnParser.parseMoves(pgn), PgnParser.parseResult(pgn)) match {
      case (Right(moves), Right(result)) => Right(Pgn(moves, result))
      case (Left(err), _) => Left(err)
      case (_, Left(err)) => Left(err)
    }
  }
}

sealed trait Result
object Result {
  case object WhiteVictory extends Result
  case object BlackVictory extends Result
  case object Draw extends Result
  case object Ongoing extends Result

  def apply(r: String): Result =
    if (r contains "1/2-1/2") {
      Draw
    } else if (r contains "0-1") {
      BlackVictory
    } else if (r contains "1-0") {
      WhiteVictory
    } else {
      Ongoing
    }
}
