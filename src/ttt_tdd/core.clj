(ns ttt-tdd.core
  (:require [clojure.repl :refer :all]
            [clojure.set :as sets]))

(def full-board (range 1 10))

(def wins #{#{1 2 3} #{4 5 6} #{7 8 9} #{1 4 7}
            #{2 5 8} #{3 6 9} #{1 5 9} #{3 5 7}})

(defn available-moves [board]
  (remove #((set board) %) full-board))

(defn x-moves [board]
  (take-nth 2 board))

(defn o-moves [board]
  (take-nth 2 (drop 1 board)))

(defn check-player-wins [board]
  (or (some #(sets/subset? % (set (x-moves board)))
            wins)
      (some #(sets/subset? % (set (o-moves board)))
            wins)))

(defn draw? [board]
  (empty? (available-moves board)))

(defn score-win-or-draw [board]
  (cond
    (check-player-wins board) 1
    (draw? board) 0))

(defn all-next-boards [board]
  (for [a (available-moves board)]
    (concat board (list a))))

(defn next-scored-boards [board]
  (for [a-board (all-next-boards board)]
    {:board a-board
     :score (if-let [win-or-draw (score-win-or-draw a-board)]
              (* (- 10 (count a-board))
                 win-or-draw)
              (->> (next-scored-boards a-board)
                   (apply max-key :score)
                   :score
                   -))}))

(defn next-board [board]
  (if (score-win-or-draw board)
    :game-over
    (->> (next-scored-boards board)
         (apply max-key :score)
         :board)))

(def mem-next-board (memoize next-board))
