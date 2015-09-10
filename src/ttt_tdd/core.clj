;; Text based command line interface

(ns ttt-tdd.core
  (:require [ttt-tdd.ai :refer [empty-board
                                next-board
                                available-moves
                                add-move-to-board
                                score-win-or-draw]]))

(defn get-input-as-integer [prompt-string]
  (try (do (println prompt-string)
           (Integer/parseInt (read-line)))
       (catch NumberFormatException e
         (get-input-as-integer prompt-string))))

(defn check-input-is-valid [input all-choices]
  (some all-choices [input]))

(defn mark-moves [vec-board moves mark]
  (apply assoc vec-board (interleave (map dec moves)
                                     (repeat mark))))

(defn printable-board [{:keys [x-moves o-moves]}]
  (let [empty-vec-board (vec (repeat 9 '_))]
    (->> (if (empty? x-moves)
           empty-vec-board
           (mark-moves empty-vec-board x-moves 'x))
         (#(if (empty? o-moves)
             %
             (mark-moves % o-moves 'o))))))

(defn print-and-return-board [board]
  (->> (printable-board board)
       (partition 3)
       (map #(interpose " " %))
       (#(interleave % (repeat "\n")))
       flatten
       (apply str)
       println)
  board)

(defn computer-play [board]
  (print-and-return-board (next-board board)))

(defn human-play [board]
  (let [available-moves (available-moves board)
        new-move (get-input-as-integer
                  (apply str
                         "Please enter a move from these available moves: "
                         (interpose " " (sort available-moves))))]
    (if (check-input-is-valid new-move available-moves)
      (print-and-return-board (add-move-to-board new-move board))
      (recur board))))

(def computer-start (cycle [computer-play human-play]))
(def human-start (drop 1 computer-start))

(defn chain-play [[first-play & others] board]
  (if (score-win-or-draw board)
    (println "GAME OVER\n")
    (recur others (first-play board))))

(defn -main []
  (let [init-choice (get-input-as-integer
                     "Here we go:
Enter 1 to start a new game with Computer starting
Enter 2 to start a new game with Human starting
Enter 3 to Exit\n")]
    (if (check-input-is-valid init-choice #{1 2 3})
      (case init-choice
        1 (do (chain-play computer-start empty-board)
              (recur))
        2 (do (chain-play human-start empty-board)
              (recur))
        3 nil)
      (recur))))
