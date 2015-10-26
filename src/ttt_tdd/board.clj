(ns ttt-tdd.board
  (:require [clojure.set :as sets]))

(def empty-board {:x-moves #{}
                  :o-moves #{}})

(def board-size (atom 4))

(def all-moves (range 1 (inc (* @board-size @board-size))))
(def row-wins (->> all-moves
                   (partition @board-size)
                   (map set)))
(def col-wins (->> all-moves
                   (partition @board-size)
                   (apply map list)
                   (map set)))
(def diag-wins (map set
                    [(take @board-size (iterate #(+ (inc @board-size) %) 1))
                     (take @board-size (iterate #(+ (dec @board-size) %) @board-size))]))

(defn wins [] (concat row-wins col-wins diag-wins))
(def mem-wins (memoize wins))

(defn available-moves [{:keys [x-moves o-moves]}]
  (sets/difference (set all-moves) x-moves o-moves))

(defn add-move-to-board [move {:keys [x-moves o-moves] :as board}]
  (cond
    (nil? move) board
    (= (count x-moves) (count o-moves)) (update board :x-moves conj move)
    :else (update board :o-moves conj move)))

(defn all-next-boards [{:keys [x-moves o-moves] :as board}]
  (for [a (available-moves board)]
    (add-move-to-board a board)))

(defn- mark-moves [vec-board moves mark]
  (apply assoc vec-board (interleave (map dec moves)
                                     (repeat mark))))

(defn line-board [{:keys [x-moves o-moves]}]
  (let [empty-vec-board (vec (repeat (* @board-size @board-size) '_))]
    (->> (if (empty? x-moves)
           empty-vec-board
           (mark-moves empty-vec-board x-moves 'x))
         (#(if (empty? o-moves)
             %
             (mark-moves % o-moves 'o))))))

(defn matrix-board [board]
  (->> (line-board board)
       (partition @board-size)))

(defn rotate-right [mat-board]
  (map reverse (apply map list mat-board)))

(defn pb [board]
  (->> board
       matrix-board
       seq
       (interpose "\n")
       println))
