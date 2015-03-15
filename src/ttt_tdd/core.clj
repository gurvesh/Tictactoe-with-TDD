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

(defn check-player-wins [board, player-moves]
  (some #(sets/subset? % (set (player-moves board)))
        wins))

(defn draw? [board]
  (empty? (available-moves board)))

(defn score-win-or-draw [board]
  (cond
    (check-player-wins board x-moves) 1
    (check-player-wins board o-moves) -1
    (draw? board) 0))

(defn game-over? [board]
  (boolean (score-win-or-draw board)))

(defn max-or-min [board]
  (if (even? (count board)) max min))

(defn max-or-min-key [board]
  (if (even? (count board)) max-key min-key))

(defn all-next-boards [board]
  (for [a (available-moves board)]
    (concat board (list a))))

(defn score-board [board]
  (if-let [win-or-draw (score-win-or-draw board)]
    (* (- 10 (count board))
       win-or-draw)
    (reduce (max-or-min board)
            (map score-board (all-next-boards board)))))

(defn next-board [board]
  (if (score-win-or-draw board)
    nil
    (let [all-boards (all-next-boards board)]
      (->> (map score-board all-boards)
           (zipmap all-boards)
           (apply (max-or-min-key board) val)
           key))))

(def mem-next-board (memoize next-board))
