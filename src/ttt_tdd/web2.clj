;; Final html interface - uses Enlive!

(ns ttt-tdd.web2
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.session :refer [wrap-session]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ttt-tdd.ai2 :refer [empty-board
                        next-board
                        add-move-to-board
                        score-win-or-draw]]
   [net.cgrand.enlive-html :as html]))

(html/deftemplate start-html "ttt_tdd/start.html" [])

(defn start-page [score]
  {:status 200
   :body (start-html)
   
   ;; Always reset board but only reset score at start of session
   :session {:current-board empty-board 
             :score (or score 
                        {:draws 0 :losses 0})}})

(html/defsnippet button "ttt_tdd/button.html"
  [:button]
  [loc]
  (html/set-attr :value (str loc)))

(html/defsnippet form "ttt_tdd/button.html"
  [:form]
  [loc]
  (html/content (button loc)))

(defn display-if [pred]
  (if pred identity (html/content nil)))

(html/deftemplate play-html "ttt_tdd/ttt2.html"
  [board score]
  [:div] (fn [match]
           (let [loc (Integer. (first (html/attr-values match :data-loc)))]
             (cond
               ;; If the location is part of x-moves, change the class
               ;; to cross and let the css do the rest
               ((:x-moves board) loc) ((html/set-attr :class "cross") match)

               ;; Same for o-moves, change the class to nought
               ((:o-moves board) loc) ((html/set-attr :class "nought") match)

               ;; If game's over then don't show anything (no form on
               ;; any loc - which could change the state if
               ;; accidentally clicked - for good score-keeping)
               (score-win-or-draw board) ((html/content nil) match)

               ;; Otherwise present a button/form with the
               ;; location's value to be submitted
               :else ((html/content (form loc)) match))))
  
  [:#restart] (display-if (score-win-or-draw board))
  [:#lose] (display-if (and (score-win-or-draw board)
                            (< 0 (score-win-or-draw board))))
  [:#draw] (display-if (= 0 (score-win-or-draw board)))
  [:p#losses] (html/content (str "Lost " (:losses score)))
  [:p#draws] (html/content (str "Draws " (:draws score))))

(defn play [board score computer-starting?]
  (let [new-board (cond
                    ;; When you go first and you finish the game
                    (score-win-or-draw board) board

                    ;; When you go first and its the first move
                    (and (not computer-starting?) 
                         (= board empty-board)) empty-board

                    ;; Otherwise get the AI to produce a new board     
                    :else (next-board board))
        new-score (condp = (score-win-or-draw new-board)
                    ;; See if computer's won or drawn, otherwise keep
                    ;; the same score
                    nil score
                    0 (update score :draws inc)
                    (update score :losses inc))]
    {:status 200
     :body (apply str (play-html new-board new-score))
     :session {:current-board new-board
               :score new-score}}))

(defn main-handler [{:keys [session params uri]}]
  (let [current-board (:current-board session)
        score (:score session)

        ;; To prevent converting null at session start, check if a
        ;; move's been made yet
        new-move  (when (get params "new-move") 
                    (Integer. (get params "new-move")))
        new-board (add-move-to-board new-move current-board)]
    (case uri
      "/" (start-page score)
      "/computer" (play new-board score true) ;; Computer goes first
      "/human" (play new-board score false)))) ;; Computer goes second

(def handler
  (-> main-handler
      wrap-params
      wrap-session
      wrap-keyword-params
      (wrap-resource "public/css")))
