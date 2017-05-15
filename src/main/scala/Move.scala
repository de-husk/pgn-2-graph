package pgn

case class Move(
  san: SAN,
  turn: Turn
)

// SAN: Standard Algebraic Notation.
case class SAN(
  turnNumber: Int,
  move: String
)

sealed trait Turn
object Turn {
  case object White extends Turn
  case object Black extends Turn
}


