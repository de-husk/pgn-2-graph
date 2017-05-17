package pgn

case class Pgn(
  moves: List[Move],
  result: GameResult
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

sealed trait GameResult
case object WhiteVictory extends GameResult
case object BlackVictory extends GameResult
case object Draw extends GameResult
case object Ongoing extends GameResult

