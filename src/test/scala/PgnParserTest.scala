package pgn

import org.specs2.mutable.Specification

class PgnParserTest extends Specification {
  val pgn = "[Tag foo] \n[Tag2 bar] \n1. e4 e5 2. d4 d5 3. exe4 d6"
  val expectedMoveLine = "1. e4 e5 2. d4 d5 3. exe4 d6"
  val fullPgn = scala.io.Source.fromFile("testdata/test.pgn").mkString

  "parseMoves" should {
    "return the correct list of turns" in {
      val moves = PgnParser.parseMoves(pgn)
      moves shouldEqual Right(Stream(
        Move(SAN(1, "e4"), White),
        Move(SAN(1, "e5"), Black),
        Move(SAN(2, "d4"), White),
        Move(SAN(2, "d5"), Black),
        Move(SAN(3, "exe4"), White),
        Move(SAN(3, "d6"), Black))
      )
    }

    "correctly handles castling notation" in {
      PgnParser.parseMoves(pgn++" 4. o-o-o") match {
        case Right(moves) =>
          moves(6) shouldEqual Move(SAN(4, "o-o-o"), White)
        }
    }

    "preserves piece pawn promoted into" in {
      PgnParser.parseMoves(pgn++" 4. f8=Q+") match {
        case Right(moves) =>
          moves(6) shouldEqual Move(SAN(4, "f8=q"), White)
      }
    }

    "ignores move commentary" in {
      PgnParser.parseMoves(fullPgn) match {
        case Right(moves) => moves.isEmpty shouldEqual false
      }
    }

    "returns MissingMoves error when no moves line is present" in {
      val error = PgnParser.parseMoves("[Result 1-0] \n [Date 12:12:02] \n [Foo Bar]\n\n\n")
      error shouldEqual Left(MissingMoves)
    }
  }

  "parseResult" should {
    "correctly parse the result" in {
      val result = PgnParser.parseResult(fullPgn)
      result shouldEqual Right(Result.WhiteVictory)
    }

    "returns MissingResult error when no Result tag is present" in {
      val error = PgnParser.parseResult("[Foo 1-0] \n [Date 12:12:02] \n [Foo Bar]\n\n\n 1. e4 d5 2. e6 d6 ")
      error shouldEqual Left(MissingResult)
    }
  }
}
