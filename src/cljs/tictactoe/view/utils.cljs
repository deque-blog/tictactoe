(ns tictactoe.view.utils)


(defn square-svg-panel
  [options]
  (let [pixel-size (:pixel-size options)
        model-size (:model-size options)]
    [:svg.board
     {:view-box (str "0 0 " model-size " " model-size)
      :style {:max-height (str pixel-size "px")}}
     ]))

(defn- special-char
  [str-code]
  [:div {:dangerouslySetInnerHTML {:__html str-code}}])

(def back-arrow (special-char "&larr;"))
(def circle-arrow (special-char "&#x21bb;"))
