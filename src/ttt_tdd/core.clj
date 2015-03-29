(ns ttt-tdd.core
  (:require [ttt-tdd.ai :refer [empty-board
                                next-board
                                available-moves
                                add-move-to-board
                                score-win-or-draw]]
            [clojure.string :as s]))

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

(defn printable-board [{:keys [x-moves o-moves] :as board}]
  (let [empty-vec-board (vec (repeat 9 '_))]
    (->> (if (empty? x-moves)
           empty-vec-board
           (mark-moves empty-vec-board x-moves 'X))
         (#(if (empty? o-moves)
             %
             (mark-moves % o-moves 'O)))
         str
         (#(subs % 1 (dec (count %)))))))

(defn print-board [board]
  (->> (printable-board board)
       ((fn [printable-board]
          [(subs printable-board 0 5)
           (subs printable-board 6 11)
           (subs printable-board 12)]))
       (#(apply str (interleave % (repeat "\n"))))
       println))

(defn computer-play [board]
  (let [next-board (next-board board)]
    (when (not= next-board :game-over)
      (do (print-board next-board)
          next-board))))

(defn human-play [board]
  (let [available-moves (available-moves board)]
    (when (seq available-moves)
      (let [new-move (get-input-as-integer
                      (apply str
                             "Please enter a move from these available moves: "
                             (interleave (sort available-moves)
                                         (repeat " "))))]
        (if (check-input-is-valid new-move available-moves)
          (let [next-board (add-move-to-board new-move board)]
            (print-board next-board)
            next-board)
          (recur board))))))

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
Enter 2 to start a new game with human starting
Enter 3 to Exit\n")]
    (if (check-input-is-valid init-choice #{1 2 3})
      (case init-choice
        1 (do (chain-play computer-start empty-board)
              (recur))
        2 (do (chain-play human-start empty-board)
              (recur))
        3 nil)
      (recur))))
