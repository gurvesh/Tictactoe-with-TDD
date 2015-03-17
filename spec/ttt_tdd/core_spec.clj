(ns ttt-tdd.core-spec
  (:require [speclj.core :refer :all]
            [ttt-tdd.core :refer :all]))

(describe "available moves"
          (it "returns the available moves given a board"
              (should= (range 1 10) (available-moves '()))
              (should= '(7 8 9) (available-moves (range 1 7)))
              (should= '() (available-moves (range 1 10)))))

(describe "game-over?"
          (it "checks if the game is over"
              (should= true (game-over? (range 9 0 -1)))
              (should= true (game-over? '(1 5 2 4 3)))
              (should-not= true (game-over? '(1 3 5 6)))
              (should= true (game-over? '(5 1 9 2 7 3)))
              (should= true (game-over? '(1 2 3 5 4 6 9 8)))))

(describe "player-moves"
          (it "returns the moves for the player to make the LAST move on board"
              (should= '(1 3 5) (player-moves '(1 2 3 4 5)))
              (should= '(2 4 6) (player-moves '(1 2 3 4 5 6)))))

#_(describe "x-moves"
          (it "returns x's moves given a board"
              (should= (x-moves full-board)
                       '(1 3 5 7 9))))

(describe "score-win-or-draw"
          (it "returns 1 if the player to make the LAST move has won,
0 if the player has drawn, otherwise nil"
              (should= 1 (score-win-or-draw '(1 2 3 5 6 7 9)))
              (should= 1 (score-win-or-draw '(1 2 3 5 4 6 9 8)))
              (should= nil (score-win-or-draw '()))
              (should= 1 (score-win-or-draw '(1 4 2 5 3)))
              (should= 1 (score-win-or-draw '(1 2 3 5 6 8)))
              (should= 0 (score-win-or-draw '(1 2 3 5 4 6 8 7 9)))))

(describe "score-board"
          (it "returns the score of a board - which is:
a) if winner or draw: (1 or -1 or 0) * (10 - number of available moves)
b) otherwise: (max or min) score of the all the next available boards"
              (should= 1 (score-board '(1 2 3 4 5 6 7 8 9)))
              (should= 5 (score-board '(1 4 2 8 3)))
              (should= 0 (score-board '(1 2 3 5 4 6 8 7 9)))
              (should= -5 (score-board '(1 4 2 8)))
              (should= -4 (score-board '(5 1 9 2 7)))
              (should= 0 (score-board '(1 5)))))

(describe "next-board"
          (it "returns the next board after scoring all possible next boards"
              (should= nil (next-board (range 1 10)))
              (should= '(1 4 2 8 3) (next-board '(1 4 2 8)))
              (should= '(1 2 3 4 5 6 7) (next-board '(1 2 3 4 5 6)))))
