# pgn-2-graph
Simple tool that transforms pgn files of chess games into a neo4j graph for analysis.

Importing multiple games will append to the existing neo4j chess graph.

## Overview

### Chess Graph 
```
 (`n`: BoardState {
    moveNumber: n,
    lastMove: "e4",
    fen: "...",
    totalGames: 10,
    whiteWinCnt: 4,
    blackWinCnt: 3
 }) -[:Move {move:["e5"]}]->  (...)
 ```
## TODO
- Import an entire folder of pgns at once
- Turn off NingWSClient debug mode that is getting set by the anormcypher library
- Attach the FEN string to each chess boardstate node


