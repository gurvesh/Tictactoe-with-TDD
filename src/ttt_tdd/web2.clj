;; Final html interface - uses Enlive!

(ns ttt-tdd.web2
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.session :refer [wrap-session]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ttt-tdd.ai :refer [empty-board
                       next-board
                       add-move-to-board
                       score-win-or-draw
                       available-moves]]
   [net.cgrand.enlive-html :as html]))

(html/deftemplate start-html "ttt_tdd/start.html" [])

(def start-page
  {:status 200
   :body (start-html)
   :session {:current-board empty-board}})

(html/defsnippet button "ttt_tdd/button.html"
  [:button]
  [id]
  (html/set-attr :value (str id)))

(html/defsnippet form "ttt_tdd/button.html"
  [:form]
  [id]
  (html/content (button id)))

(defn display-if [pred]
  (if pred
    identity
    (html/content nil)))

(html/deftemplate play-html "ttt_tdd/ttt.html"
  [board]
  [:div] (fn [match]
           (let [id (Integer. (first (html/attr-values match :id)))]
             (cond
               ((:x-moves board) id) ((html/set-attr :class "cross") match)
               ((:o-moves board) id) ((html/set-attr :class "nought") match)
               :else ((html/content (form id)) match))))
  [:#restart] (display-if (score-win-or-draw board))
  [:#lose] (display-if (= 1 (score-win-or-draw board)))
  [:#draw] (display-if (= 0 (score-win-or-draw board))))

(defn play-page [board computer-starting?]
  (let [new-board (cond
                    (score-win-or-draw board) board
                    (and (not computer-starting?)
                         (= board empty-board)) empty-board
                    :else (next-board board))]
    {:status 200
     :body (apply str (play-html new-board))
     :session {:current-board new-board}}))

(defn main-handler [{:keys [session
                            params
                            uri]
                     :as request}]
  (let [current-board (or (:current-board session)
                          empty-board)
        new-move  (when (get params "new-move")
                    (Integer. (get params "new-move")))
        new-board (if (score-win-or-draw current-board)
                    current-board
                    (add-move-to-board new-move current-board))]
    (case uri
      "/" start-page
      "/computer" (play-page new-board true)
      "/human" (play-page new-board false))))

(def handler
  (-> main-handler
      wrap-params
      wrap-session
      wrap-keyword-params
      (wrap-resource "public/css")))
