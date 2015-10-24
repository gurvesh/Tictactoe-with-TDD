(ns ttt-tdd.ai2
  (:require [clojure.repl :refer :all]
            [clojure.set :as sets]
            [ttt-tdd.core :refer [mark-moves]]))

(def empty-board {:x-moves #{}
                  :o-moves #{}})

(def board-size 4)

(def all-moves (range 1 (inc (* board-size board-size))))
(def row-wins (->> all-moves
                   (partition board-size)
                   (map set)))
(def col-wins (->> all-moves
                   (partition board-size)
                   (apply map list)
                   (map set)))
(def diag-wins (map set
                    [(take board-size (iterate #(+ (inc board-size) %) 1))
                     (take board-size (iterate #(+ (dec board-size) %) board-size))]))

(def wins  (concat row-wins col-wins diag-wins))

(defn available-moves [{:keys [x-moves o-moves]}]
  (sets/difference (set all-moves) x-moves o-moves))

(defn check-player-wins [{:keys [x-moves o-moves]}]
  (or (some #(sets/subset? % x-moves) wins)
      (some #(sets/subset? % o-moves) wins)))

(defn draw? [board]
  (empty? (available-moves board)))

(defn score-win-or-draw [board]
  (cond
    (check-player-wins board) ((comp inc count available-moves) board)
    (draw? board) 0))

(defn add-move-to-board [move {:keys [x-moves o-moves] :as board}]
  (cond
    (nil? move) board
    (= (count x-moves) (count o-moves)) (update board :x-moves conj move)
    :else (update board :o-moves conj move)))

(defn all-next-boards [{:keys [x-moves o-moves] :as board}]
  (for [a (available-moves board)]
    (add-move-to-board a board)))

(defn line-board [{:keys [x-moves o-moves]}]
  (let [empty-vec-board (vec (repeat (* board-size board-size) '_))]
    (->> (if (empty? x-moves)
           empty-vec-board
           (mark-moves empty-vec-board x-moves 'x))
         (#(if (empty? o-moves)
             %
             (mark-moves % o-moves 'o))))))

(defn matrix-board [board]
  (->> (line-board board)
       (partition board-size)))

(defn rotate-right [mat-board]
  (map reverse (apply map list mat-board)))

(def scored-boards-in-matrix-form (atom {}))

(defn score-from-rotated-board [mat-board]
  (loop [current-board mat-board
         n 4]
    (if (zero? n)
      nil
      (if-let [found-score (get @scored-boards-in-matrix-form
                                current-board)]
        found-score
        (recur (rotate-right current-board)
               (dec n))))))

(defn score-from-similar-board [mat-board]
  (let [mirror-mat-board (map reverse mat-board)]
    (or
     (score-from-rotated-board mat-board)
     (score-from-rotated-board mirror-mat-board))))

(defn get-easy-score [board mat-board]
  (or
   (score-win-or-draw board)
   (score-from-similar-board mat-board)))

(defn store-matrix-board-and-score [mat-board score]
  (swap! scored-boards-in-matrix-form
         assoc mat-board score))

(declare alpha-beta-minimax)

(defn score-and-store-result [board achievable cutoff]
  (let [mat-board (matrix-board board)]
    (or
     (get-easy-score board mat-board)
     (let [score (-> board
                     all-next-boards
                     (alpha-beta-minimax (- cutoff) (- achievable))
                     second
                     -)]
       (store-matrix-board-and-score mat-board score)
       score))))

(defn alpha-beta-minimax [boards achievable cutoff]
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
  (cond
    (score-win-or-draw board) :game-over

    (and (> board-size 3)
         (> ((comp count available-moves) board) 12))
    (add-move-to-board (rand-nth (seq (available-moves board))) board)

    :else (first (alpha-beta-minimax (all-next-boards board) -1000 1000))))
