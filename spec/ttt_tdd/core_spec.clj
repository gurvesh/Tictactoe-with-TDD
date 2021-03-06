(ns ttt-tdd.core-spec
  (:require [speclj.core :refer :all]
            [ttt-tdd.board :refer :all]
            [ttt-tdd.ai :refer :all]
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
              (should (< 0 (score-win-or-draw {:x-moves #{4 6 7}
                                             :o-moves #{1 2 3 5}})))
              (should= nil (score-win-or-draw '()))
              (should= 0 (score-win-or-draw {:x-moves #{1 3 6 4 8}
                                             :o-moves #{2 5 7 9}}))))

(describe "next-board"
          (it "returns the next board after scoring all possible next boards"
              (should= :game-over (next-board {:x-moves #{1 2 3 4 5}
                                               :o-moves #{6 7 8 9}}))

              (should= {:x-moves #{1 2 3} :o-moves #{4 8}}
                       (next-board {:x-moves #{1 2} :o-moves #{4 8}}))

              (should= {:x-moves #{1 3 5 9} :o-moves #{2 4 7}}
                       (next-board {:x-moves #{1 3 5} :o-moves #{2 4 7}}))

              ;; Because of randomisation the following is no longer
              ;; always true
              
              #_(should= (next-board {:x-moves #{1 3 5} :o-moves #{2 4 6}})
                       (next-board {:x-moves #{5 3 1} :o-moves #{4 2 6}}))))

(describe "get-input-as-integer"
          (it "prompts the player for an input, and succeeds only if an integer
is input. If anything else is encountered, it should recur."
              (should= 1 (with-in-str "1" (get-input-as-integer "test")))
              (should= "test\n" (with-out-str (with-in-str "1"
                                                (get-input-as-integer "test"))))
              (should= 2 (with-in-str "hi \n2" (get-input-as-integer "test2")))
              (should= 5 (with-in-str "hi \nhi \nhi \n5"
                           (get-input-as-integer "get me an integer")))))

(describe "check-input-is-valid"
          (it "checks that the input (which is an integer) lies within
 a provided set"
              (should= nil (check-input-is-valid 1 #{0}))
              (should= 2 (check-input-is-valid 2 (set (range 3))))))

(describe "add-move-to-board"
          (it "adds the given move to the board"
              (should= empty-board (add-move-to-board nil empty-board))
              (should= {:x-moves #{1} :o-moves #{}}
                       (add-move-to-board 1 empty-board))
              (should= {:x-moves #{1 3 4} :o-moves #{2 5 7}}
                       (add-move-to-board 7 {:x-moves #{1 3 4} :o-moves #{2 5}}))))

(describe "line-board"
          (it "should get a line string for the board"
              (should= '[_ _ _ _ _ _ _ _ _] (line-board empty-board))
              (should= '[x x x x x x x x x]
                       (line-board {:x-moves (set (range 1 10))
                                     :o-moves #{}}))
              (should= '[x _ x _ x x _ _ x]
                       (line-board {:x-moves #{1 3 5 6 9} :o-moves #{}}))
              (should= '[x o x _ o x _ o x]
                       (line-board  {:x-moves #{1 3 6 9}
                                          :o-moves #{2 5 8}}))))

(describe "print-and-return-board"
          (it "pretty prints the board in the classic format,
and returns the board"
              (should= "x o x\n_ o x\n_ o x\n\n"
                       (with-out-str
                         (print-and-return-board {:x-moves #{1 3 6 9}
                                       :o-moves #{2 5 8}})))
              (should= {:x-moves #{1 3 5}
                        :o-moves #{2 4 7}}
                       (print-and-return-board {:x-moves #{1 3 5}
                                     :o-moves #{2 4 7}}))))

(describe "computer-play"
          (it "returns the next-board after printing it, on the computer's move"
              (should= :game-over (computer-play
                                   {:x-moves #{1 2 3 4 5}
                                    :o-moves #{6 7 8 9}}))
              (should= {:x-moves #{1 3 5 9}
                        :o-moves #{2 4 7}}
                       (computer-play {:x-moves #{1 3 5}
                                              :o-moves #{2 4 7}}))
              (should= "x o x\no x _\no _ x\n\n"
                       (with-out-str
                         (computer-play {:x-moves #{1 3 5}
                                                :o-moves #{2 4 7}})))))

(describe "human-play"
          (it "returns the next-board after printing it, on the human's move"
              (should= {:x-moves #{1 2 3} :o-moves #{4 5}}
                       (with-in-str "hi\nhi\nhi\n120\n3"
                         (human-play {:x-moves #{1 2}
                                      :o-moves #{4 5}})))))

(describe "chain-play"
          (it "checks if the board is already over. If it isn't - it gets
the next player to move"
              (should= "GAME OVER\n\n"
                       (with-out-str
                         (chain-play human-start {:x-moves #{1 2 3 4 5}
                                                  :o-moves #{6 7 8 9}})))
              (should= "GAME OVER\n\n"
                       (with-out-str
                         (chain-play computer-start {:x-moves #{1 2 3 4 5}
                                                     :o-moves #{6 7 8 9}})))))
