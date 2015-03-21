(ns ttt-tdd.core
  (:require [clojure.repl :refer :all]
            [clojure.set :as sets]))

(declare mem-next-scored-boards)

(def full-board (range 1 10))

(def wins #{#{1 2 3} #{4 5 6} #{7 8 9} #{1 4 7}
            #{2 5 8} #{3 6 9} #{1 5 9} #{3 5 7}})

(defn available-moves [{:keys [x-moves o-moves]}]
  (sets/difference (set full-board) x-moves o-moves))

(defn check-player-wins [{:keys [x-moves o-moves]}]
  (or (some #(sets/subset? % x-moves) wins)
      (some #(sets/subset? % o-moves) wins)))

(defn draw? [board]
  (empty? (available-moves board)))

(defn score-win-or-draw [board]
  (cond
    (check-player-wins board) 1
    (draw? board) 0))

(defn all-next-boards [{:keys [x-moves o-moves] :as board}]
  (for [a (available-moves board)]
    (if (= (count x-moves) (count o-moves))
      {:x-moves (conj x-moves a)
       :o-moves o-moves}
      {:x-moves x-moves
       :o-moves (conj o-moves a)})))

(defn next-scored-boards [board]
  (for [a-board (all-next-boards board)]
    {:board a-board
     :score (if-let [win-or-draw (score-win-or-draw a-board)]
              (* (inc (count (available-moves board)))
                 win-or-draw)
              (->> (mem-next-scored-boards a-board)
                   (apply max-key :score)
                   :score
                   -))}))

(def mem-next-scored-boards (memoize next-scored-boards))

(defn next-board [board]
  (if (score-win-or-draw board)
    :game-over
    (->> (mem-next-scored-boards board)
         (apply max-key :score)
         :board)))
