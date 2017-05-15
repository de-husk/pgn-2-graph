package pgn

case class Move(
  san: SAN,
  turn: Turn
) {}

sealed trait Turn
object Turn {
  case object White extends Turn
  case object Black extends Turn
}

// SAN:
// Standard Algebraic Notation.
// https://en.wikipedia.org/wiki/Algebraic_notation_(chess)
case class SAN(
  turnNumber: Int,
  move: String
)




