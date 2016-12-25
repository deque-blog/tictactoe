(ns tictactoe.game-test
  (:require-macros
    [cljs.test :refer (is deftest testing)]
    [clojure.test.check.clojure-test :refer [defspec]]
    )
  (:require
    ;;[tictactoe.common-test]
    [cljs.test :as test]
    [clojure.test.check :as tc]
    [clojure.test.check.generators :as gen]
    [clojure.test.check.properties :as prop :include-macros true]
    [tictactoe.logic.constants :as cst]
    [tictactoe.logic.game :as logic]
    ))


;; ----------------------------------------------------------------------------
;; Utils for the tests
;; ----------------------------------------------------------------------------

(defn get-cells
  [game-state]
  (map second (logic/get-board game-state)))

(defn count-if
  [pred coll]
  (count (filter pred coll)))

(defn count-equal
  [value coll]
  (count-if #(= % value) coll))

(defn count-cells
  [cell-type game-state]
  (count-equal cell-type (get-cells game-state)))

(defn count-empty-cells
  [game-state]
  (count-cells :cell/empty game-state))

(defn play-moves
  [init-game moves]
  (reduce
    (fn [game [x y]] (logic/on-move game x y))
    init-game
    moves))


;; ----------------------------------------------------------------------------
;; Generators
;; ----------------------------------------------------------------------------

(def coord-gen
  (gen/elements cst/coordinates))

(def player-gen
  (gen/elements #{:cell/cross :cell/circle}))

(def cell-gen
  (gen/elements #{:cell/empty :cell/cross :cell/circle}))

;; Board gen is not the way to go => does not allow to have valid states
;; And you would have to generate too much things (game, etc...)
(def board-gen
  (apply gen/hash-map
    (interleave
      cst/coordinates
      (repeat cst/cell-count cell-gen)
      )))

#_(def game-gen
  (gen/let [nb (gen/int)
            xy (gen/sample coord-gen nb)]
    (play-moves (logic/new-game) xy)
    ))


;; ----------------------------------------------------------------------------
;; Example based tests
;; ----------------------------------------------------------------------------

(deftest test-init-game
  (let [init-game (logic/new-game)]
    (is (not (logic/game-over? init-game)))
    (is (= cst/cell-count (count-empty-cells init-game)))
    ))

(deftest test-game-over
  (let [init-game (logic/new-game)
        end-game (play-moves init-game cst/coordinates)]
    (is (logic/game-over? end-game))
    (is (> cst/cell-count (count-empty-cells end-game)))
    ))

(deftest test-undo-game
  (let [init-game (logic/new-game)
        end-game (play-moves init-game cst/coordinates)
        undo-game (reduce #(logic/on-undo %1) end-game cst/coordinates)]
    (is (logic/game-over? end-game))
    (is (= init-game undo-game))
    ))


;; ----------------------------------------------------------------------------
;; Generative testing
;; ----------------------------------------------------------------------------

(defn valid-next-game?
  [old-game new-game]
  (let [next-player (logic/get-next-player new-game)]
    (and
      (not= (logic/get-next-player old-game) next-player)
      (= (dec (count-empty-cells old-game)) (count-empty-cells new-game))
      (= (count-cells next-player old-game) (count-cells next-player new-game))
      )))

(defn valid-move-properties
  [old-game]
  (prop/for-all [[x y] coord-gen]
    (let [new-game (logic/on-move old-game x y)]
      (or (= old-game new-game) (valid-next-game? old-game new-game))
      )))

(defspec next-player-at-start 100
  (valid-move-properties (logic/new-game)))

(defspec try-move-for-any-board 100
  (prop/for-all [coords (gen/vector coord-gen 0 cst/cell-count)]
    (valid-move-properties (play-moves (logic/new-game) coords))
    ))


;; ----------------------------------------------------------------------------
;; Running the tests
;; ----------------------------------------------------------------------------

(test/run-tests)