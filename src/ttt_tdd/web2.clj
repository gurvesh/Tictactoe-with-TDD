(ns ttt-tdd.web2
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.session :refer [wrap-session]]
   [ring.middleware.resource :refer [wrap-resource]]
   [hiccup.core :refer [html]]
   [ttt-tdd.ai :refer [next-board
                       empty-board
                       add-move-to-board
                       score-win-or-draw
                       available-moves]]
   [net.cgrand.enlive-html :as h]
   [clojure.set :as sets]))

(def start-page
  {:status 200
   :body (html
          [:p "Click one of the two links to get started"]
          [:a {:href "/computer"} "Start new game with Computer starting"])
   :session {:current-board empty-board}})

(h/defsnippet button "ttt_tdd/button.html" [:form] [] identity)

(h/deftemplate index "ttt_tdd/ttt.html"
  [board]
  [:div] (fn [match]
           (let [id (Integer. (first (h/attr-values match :id)))]
             (cond
               ((:x-moves board) id) ((h/set-attr :class "cross") match)
               ((:o-moves board) id) ((h/set-attr :class "nought") match)
               :else ((h/content (apply (h/set-attr :value (str id)) (button))) match)))))

(defn computer-page [board]
  (let [new-board (when-not (score-win-or-draw board)
                    (next-board board))]
    {:status 200
     :body (apply str (index (or new-board board)))
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
      "/computer" (computer-page new-board))))

(def handler
  (-> main-handler
      wrap-params
      wrap-session
      wrap-keyword-params
      (wrap-resource "public/css")))


