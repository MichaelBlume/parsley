;   Copyright (c) Christophe Grand. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns net.cgrand.parsley.demo
  (:use net.cgrand.parsley :reload)
  (:use [net.cgrand.parsley.internal :as core] :reload))

;; TRY AT YOUR OWN RISK :-)

(def simple-lisp 
  (grammar {:space [" "+]
            :main :expr*} 
    :eot- (with #{(one-of "()") eof})
    :expr #{:symbol ["(" :expr* ")"] :nil}
    :nil (token "nil" :eot)
    :symbol (token (but :nil) {\a \z \A \Z \- \-} {\a \z \A \Z \0 \9 \- \-}*)))   

(-> simple-lisp (step "a") count)

(defmulti str-op first)
(defmethod str-op :default [[op]] (str"?" op "?"))
(defmethod str-op core/op-cat [[_ & xs]] (str "[" (apply str (interpose " " (map str-op xs))) "]"))
(defmethod str-op core/op-alt [[_ & xs]] (str "#{" (apply str (interpose ", " (map str-op xs))) "}"))
(defmethod str-op core/op-string [[_ s]] (pr-str s))
(defmethod str-op core/op-repeat [[_ op]] (str (str-op op) "*"))

(defn str-ops [ops] (apply str (map str-op ops))) 

  

(def clojure-parser 
  (grammar {:space #{:white-space :comment :discard}
            :main :expr*}

    :terminating-macro- (one-of "\";']^`~()[]{}\\%")
    :space- (one-of " \t\n\r,")
    :eot- (with #{:terminating-macro :space eof})

    :white-space (token :space+)  
    :comment (token #{";" "#!"} [(but "\n") any-char]*) 
    :discard ["#_" :expr]
    
    :expr #{:list :vector :map :set :fn 
            :meta :with-meta :quote :syntax-quote :unquote :unquote-splice
            :regex :string :number :keyword :symbol :nil :boolean :char}

    :list ["(" :expr* ")"] 
    :vector ["[" :expr* "]"] 
    :map ["{" [:expr :expr]* "}"]
    :set ["#{" :expr* "}"]
    :fn ["#(" :expr* ")"]

    :meta ["^" :expr]
    :with-meta ["#^" :expr :expr]
    :quote ["'" :expr] 
    :syntax-quote ["`" :expr]
    :unquote ["~" :expr] 
    :unquote-splice ["~@" :expr]
    :deref ["@" :expr]
    :var ["#'" :expr]

    :nil (token "nil" :eot)
    :boolean (token #{"true" "false"} :eot)
    ;; todo: refine these terminals
    :char (token \\ any-char)
    :namespace- (token (not-one-of "/" {\0 \9}) [(but :eot) any-char]* "/")
    :symbol (token (but :nil :boolean) 
              #{"/"
                "clojure.core//"
                [:namespace? {\a \z \A \Z}+]} :eot)  
    :keyword (token ":" {\a \z \A \Z}+ :eot)
    :number (token {\0 \9}+)
    :string (token \" #{[(but \" \\) any-char] [\\ any-char]}* \")
    :regex (token "#\"" #{[(but \" \\) any-char] [\\ any-char]}* \")))
    
(def test-comment
  (grammar {:main :expr* :space [(one-of " \t\n\r,")+]}
    :expr [:line :comment]
    :line [[(but ";") {\a \z}]*]
    :comment (token ";" [(but "\n") any-char]*)))  

(-> test-comment (step "a ;b\nc;d") (step nil) results (->> (map #(apply str (e/emit* %)))))

(defn find-error [s g]
  (loop [seen [] s s p g]
    (if (empty? p)
      (apply str seen)
      (when-let [[c & s] (seq s)]
        (recur (conj seen c) s (step p (str c)))))))
  
  if (and (seq s) (-> f (step s) count zero?))
    (let [i (quot (count s) 2)
          s1 (subs s 0 i)
          s2 (subs s i)
          ]
      (if (-> f (step s1) count zero?)
        
        
      
    

;; helper functions to display results in a more readable way 
(defn terse-result [items]
  (map (fn self [item]
         (if (map? item)
           (cons (:tag item) (map self (:content item)))
           item)) items))

(defn prn-terse [results]
  (doseq [result results]
    (prn (terse-result result))))
    
;; let's parse this snippet
(-> simple-lisp (step "()(hello)") (step nil) results prn-terse)
;;> ((:main (:expr "()") (:expr "(" (:expr (:symbol "hello")) ")")))

;; let's parse this snippet in two steps
(-> simple-lisp (step "()(hel") (step "lo)") eof results prn-terse)
;;> ((:main (:expr "()") (:expr "(" (:expr (:symbol "hello")) ")")))

;; and now, the incremental parsing!
(let [c1 (-> simple-lisp reset (step "()(hel"))
      c2 (-> c1 reset (step "lo)" nil))
      _ (-> (stitch c1 c2) eof results prn-terse) ; business as usual
      c1b (-> simple-lisp reset (step "(bonjour)(hel")) ; an updated 1st chunk
      _ (-> (stitch c1b c2) eof results prn-terse) 
      c1t (-> simple-lisp reset (step "(bonjour hel")) ; an updated 1st chunk
      _ (-> (stitch c1t c2) eof results prn-terse)] 
  nil)
;;> ((:main (:expr "()") (:expr "(" (:expr (:symbol "hello")) ")")))
;;> ((:main (:expr "(" (:expr (:symbol "bonjour")) ")") (:expr "(" (:expr (:symbol "hello")) ")")))
;;> ((:main (:expr "(" (:expr (:symbol "bonjour")) (:w " ") (:expr (:symbol "hello")) ")")))

    