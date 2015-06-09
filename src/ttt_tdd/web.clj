(ns ttt-tdd.web
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.session :refer [wrap-session]]
   [hiccup.core :refer [html]]
   [hiccup.form :refer [form-to
                        text-field
                        submit-button]]
   [ttt-tdd.ai :refer [next-board
                       empty-board
                       add-move-to-board
                       score-win-or-draw]]
   [ttt-tdd.core :refer [print-and-return-board]]))

(declare handler
         main-handler
         start-page
         computer-page
         human-page)

(defonce server
  (jetty/run-jetty #'handler {:port 5000
                              :join? false}))

(def handler
  (-> main-handler
      wrap-params
      wrap-session
      wrap-keyword-params))

(defn main-handler [{:keys [session
                            params
                            uri]
                     :as request}]
  (let [current-board (or (:current-board session)
                          empty-board)
        new-move (when (get params "new-move")
                   (Integer. (get params "new-move")))
        new-board (next-board (add-move-to-board new-move current-board))]
    (case uri
      "/" start-page
      "/computer" (computer-page new-board)
      "/human" (human-page new-board) ;still to implement
      )))

(def start-page
  {:status 200

   :body
   (html
    [:p "Click one of the two links to get started"]
    [:a {:href "/computer"} "Start new game with Computer starting"]
    [:br]
    [:a {:href "/human"} "Start new game with Human starting"])

   :session {:current-board empty-board}})

(defn computer-page [board]
  {:status 200

   :body
   (html
    [:p (with-out-str (print-and-return-board board))]
    [:br]
    (form-to [:post ""]
             (text-field "new-move")
             (submit-button "Next move")))

   :session (if (score-win-or-draw board)
              {:current-board empty-board}
              {:current-board board})})
