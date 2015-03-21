(ns ttt-tdd.core-spec
  (:require [speclj.core :refer :all]
            [ttt-tdd.core :refer :all]))

(describe "available moves"
          (it "returns the available moves given a board"
              (should= (set (range 1 10)) (available-moves '()))
              (should= #{7 8 9} (available-moves {:x-moves #{1 2 3}
                                                  :o-moves #{4 5 6}}))
              (should= #{} (available-moves {:x-moves #{1 2 3 4 5}
                                             :o-moves #{6 7 8 9}}))))

(describe "score-win-or-draw"
          (it "returns 1 if a player has won, or 0 if draw"
              (should= 1 (score-win-or-draw {:x-moves #{1 2 3 4 5}
                                             :o-moves #{6 7 8 9}}))
              (should= 1 (score-win-or-draw {:x-moves #{4 6 7}
                                             :o-moves #{1 2 3 5}}))
              (should= nil (score-win-or-draw '()))
              (should= 0 (score-win-or-draw {:x-moves #{1 3 6 4 8}
                                             :o-moves #{2 5 7 9}}))))

(describe "next-board"
          (it "returns the next board after scoring all possible next boards"
              (should= :game-over (next-board {:x-moves #{1 2 3 4 5}
                                               :o-moves #{6 7 8 9}}))
              (should= {:x-moves #{1 2 3} :o-moves #{4 8}}
                       (next-board {:x-moves #{1 2} :o-moves #{4 8}}))
              (should= {:x-moves #{1 3 5 9} :o-moves #{2 4 6}}
                       (next-board {:x-moves #{1 3 5} :o-moves #{2 4 6}}))))
