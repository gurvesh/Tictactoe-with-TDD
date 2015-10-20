(ns ttt-tdd.ai2
  (:require [clojure.repl :refer :all]
            [clojure.set :as sets]))

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
    (= (count x-moves) (count o-moves))  {:x-moves (conj x-moves move)
                                          :o-moves o-moves}
    :else {:x-moves x-moves
           :o-moves (conj o-moves move)}))

(defn all-next-boards [{:keys [x-moves o-moves] :as board}]
  (for [a (available-moves board)]
    (add-move-to-board a board)))

(defn mark-moves [vec-board moves mark]
  (apply assoc vec-board (interleave (map dec moves)
                                     (repeat mark))))

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

(defn score-from-similar-board [board]
  (let [mat-board (matrix-board board)
        mirror-mat-board (map reverse mat-board)]
    (or
     (score-from-rotated-board mat-board)
     (score-from-rotated-board mirror-mat-board))))

(defn get-easy-score [board]
  (or
   (score-win-or-draw board)
   (score-from-similar-board board)))

(defn store-matrix-board-and-score [board score]
  (swap! scored-boards-in-matrix-form
         assoc (matrix-board board) score))

(declare next-scored-boards)

(defn minimax-and-store-result [board achievable cutoff]
  (let [boards-not-pruned (next-scored-boards board achievable cutoff)
        score (->> (apply max-key val boards-not-pruned)
                   val
                   -)]
    (store-matrix-board-and-score board score)
    score))

(defn next-scored-boards [board achievable cutoff]
  (loop [remaining-boards (all-next-boards board)
         achievable achievable
         results {}]
    (if (nil? remaining-boards)
      results
      (let [current-board (first remaining-boards)
            score (or (get-easy-score current-board)
                      (minimax-and-store-result current-board
                                                (- cutoff)
                                                (- achievable)))
            new-results (conj results [current-board score])]
        (cond
          (>= achievable cutoff) new-results
          (> score achievable) (recur (next remaining-boards)
                                      score
                                      new-results)
          :else (recur (next remaining-boards)
                       achievable
                       new-results))))))

(defn next-board [board]
  (do
    (swap! scored-boards-in-matrix-form empty)
    (cond
      (score-win-or-draw board) :game-over
      (= empty-board board) (update board :x-moves conj (rand-nth all-moves))
      :else (let [all-boards (next-scored-boards board -1000 1000)
                  max-score (val (apply max-key val all-boards))
                  all-boards-with-max-score (filter #(= max-score
                                                        (val %))
                                                    all-boards)]
              (key (rand-nth all-boards-with-max-score))))))
