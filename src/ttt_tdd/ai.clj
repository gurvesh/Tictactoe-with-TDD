(ns ttt-tdd.ai
  (:require [clojure.set :as sets]
            [ttt-tdd.board :refer :all]))

(def scored-boards-in-matrix-form (atom {}))

(defn- check-player-wins [{:keys [x-moves o-moves]}]
  (or (some #(sets/subset? % x-moves) wins)
      (some #(sets/subset? % o-moves) wins)))

(defn- draw? [board]
  (empty? (available-moves board)))

(defn score-win-or-draw [board]
  (cond
    (check-player-wins board) ((comp inc count available-moves) board)
    (draw? board) 0))

(defn- score-from-rotated-board [mat-board]
  (loop [current-board mat-board
         n 4]
    (if (zero? n)
      nil
      (if-let [found-score (get @scored-boards-in-matrix-form
                                current-board)]
        found-score
        (recur (rotate-right current-board)
               (dec n))))))

(defn- score-from-similar-board [mat-board]
  (or (score-from-rotated-board mat-board)
      (score-from-rotated-board (map reverse mat-board))))

(defn- get-easy-score [board mat-board]
  (or (score-win-or-draw board)
      (score-from-similar-board mat-board)))

(defn- store-matrix-board-and-score [mat-board score]
  (swap! scored-boards-in-matrix-form
         assoc mat-board score))

(declare alpha-beta-minimax)

(defn- score-and-store-result [board achievable cutoff]
  (let [mat-board (matrix-board board)]
    (or (get-easy-score board mat-board)
        (let [score (-> board
                        all-next-boards
                        (alpha-beta-minimax (- cutoff) (- achievable))
                        second
                        -)]
          (store-matrix-board-and-score mat-board score)
          score))))

(defn- alpha-beta-minimax [boards achievable cutoff]
  (loop [boards boards
         achievable achievable
         best-board (first boards)]
    (if (or  (nil? boards)
             (>= achievable cutoff))
      [best-board achievable]
      (let [current-board (first boards)
            score (score-and-store-result current-board achievable cutoff)]
        (recur (next boards)
               (if (> score achievable) score achievable)
               (if (> score achievable) current-board best-board))))))

(defn next-board [{:keys [x-moves o-moves] :as board}]
  (do
    (swap! scored-boards-in-matrix-form empty)
    (cond
      (score-win-or-draw board) :game-over

      (and (> @board-size 3)
           (> ((comp count available-moves) board) 12))
      (add-move-to-board (rand-nth (seq (available-moves board))) board)

      :else (first (alpha-beta-minimax (all-next-boards board) -1000 1000)))))
