import pgn.Pgn

// Usage:
// `Pgn2Graph.main(Array($path_to_pgn_file))`

import scala.io.Source._
import java.nio.file.{Files, Paths}
import org.anormcypher._
import play.api.libs.ws._
import scala.concurrent.ExecutionContext
import com.typesafe.config.ConfigFactory

object Pgn2Graph {
  implicit val ec = ExecutionContext.global
  implicit val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    val pgnPath = args(0)
    val pgn = fromFile(pgnPath).mkString

    val p = Pgn(pgn) match {
      case Right(p1) => p1
      case Left(parseError) =>
        println("PARSE ERROR: " + parseError)
        sys.exit(1)
    }
    println("Processing [" + pgnPath + "]")
    createRoot()
    insertPgnIntoGraph(p)
  }

  private def createRoot(): Unit = {
    implicit val wsclient = ning.NingWSClient()
    implicit val connection = getNeo4jConnection(wsclient)

    val initialBoardState = Cypher("""
       MATCH (b:BoardState)
       WHERE b.moveNumber = 0
       return 1
     """).apply()

    if (initialBoardState.length == 0) {
      val result = Cypher("""
         create (`0`:BoardState {
             moveNumber: 0,
             fen: "TODO",
             lastMove: "",
             totalGames: 0,
             blackWinCount: 0,
             whiteWinCount: 0,
             drawWinCount: 0
         })
      """).execute()
    }
    wsclient.close()
  }

  private def insertPgnIntoGraph(pgn: Pgn): Unit = {
    implicit val wsclient = ning.NingWSClient()
    implicit val connection = getNeo4jConnection(wsclient)

    println("Inserting into neo4j graph:\n" + pgn)

    pgn.moves.zipWithIndex.sliding(2).foreach { turn =>
      val prev = turn.head._1
      val prevIndex = turn.head._2 + 1
      val cur = turn.last._1
      val curIndex = turn.last._2 + 1

      val prevNode = Cypher("""
         MATCH(b:BoardState)
         WHERE b.moveNumber = {moveNumber} and b.lastMove = {prevMove}
         return 1
      """).on(
        "moveNumber" -> prevIndex,
        "prevMove" -> prev.san.move
      ).apply()

      if (prevNode.length == 0) {
        // Connect root -> prev:
        Cypher("""
            MATCH (b:BoardState)
            WHERE b.moveNumber = 0
            CREATE UNIQUE (b) -[:MOVE {move:[{move}]}]-> (
               `{moveNumber}`:BoardState {
               moveNumber: {moveNumber},
               fen: "TODO",
               lastMove: {move},
               totalGames: 1
            })
         """).on(
          "move" -> prev.san.move,
          "moveNumber" -> prevIndex
        ).execute()
      }

      // Connect prev -> cur:
      Cypher("""
            MATCH (b:BoardState)
            WHERE
                b.moveNumber = {prevMoveNum} and
                b.lastMove = {prevMove}
            CREATE UNIQUE (b) -[:MOVE {move:[{move}]}]-> (
               `{moveNumber}`:BoardState {
               moveNumber: {moveNumber},
               fen: "TODO",
               lastMove: {move},
               totalGames: 1
            })
         """).on(
        "prevMove" -> prev.san.move,
        "prevMoveNum" -> prevIndex,
        "move" -> cur.san.move,
        "moveNumber" -> curIndex
      ).execute()
    }
    wsclient.close()
  }

  private def getNeo4jConnection(implicit wsclient: ning.NingWSClient): Neo4jConnection = Neo4jREST(
    config.getString("neo4j.host"),
    config.getInt("neo4j.port"),
    config.getString("neo4j.username"),
    config.getString("neo4j.password")
  )

}
