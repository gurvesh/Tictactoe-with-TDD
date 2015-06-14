(ns ttt-tdd.web
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.session :refer [wrap-session]]
   [hiccup.core :refer [html]]
   [hiccup.form :refer [form-to
                        text-field
                        submit-button
                        drop-down]]
   [ttt-tdd.ai :refer [next-board
                       empty-board
                       add-move-to-board
                       score-win-or-draw
                       available-moves]]
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
        new-board (if (score-win-or-draw current-board)
                    current-board
                    (add-move-to-board new-move current-board))]
    (case uri
      "/" start-page
      "/computer" (computer-page new-board)
      "/human" (human-page current-board))))

(def start-page
  {:status 200
   :body (html
          [:p "Click one of the two links to get started"]
          [:a {:href "/computer"} "Start new game with Computer starting"]
          [:br]
          [:a {:href "/human"} "Start new game with Human starting"])
   :session {:current-board empty-board}})

(defn- split-into-rows [board]
  (-> (print-and-return-board board)
      with-out-str
      (clojure.string/split #"[\n]")
      (#(interleave [:p [:br] [:br]] %))
      vec))

(defn submit-move-form [board]
  (form-to [:post "/computer"]
           (drop-down "new-move" (sort (available-moves board)))
           (submit-button "Next move")))

(defn computer-page [board]
  (let [new-board (when-not (score-win-or-draw board)
                    (next-board board))]
    {:status 200
     :body (html
            [:p "I received this board:"]
            (split-into-rows board)
            [:br]
            (when-not (score-win-or-draw board)
              [:p "Here's a board for you after my move:"])
            (when-not (score-win-or-draw board)
              (split-into-rows new-board))
            [:br] 
            (if (or (score-win-or-draw board)
                    (score-win-or-draw new-board))
              [:p "Game Over" [:br]
               [:a {:href "/"} "Go back to main menu to start a new game"]]
              (submit-move-form new-board)))
     :session {:current-board new-board}}))

(defn human-page [board]
  {:status 200
   :body (html
          [:p "Make the first move"]
          (split-into-rows empty-board)
          (submit-move-form board))})
