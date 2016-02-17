(ns syntest.core)

(defmacro syn-run! [& parts]
  `(do (cljs.test/async
        ~'done
        (let [~'runner 
              ~@(list (concat [`cljs.core.async.macros/go]
                              parts [`(stop-current-test!)]))] 
          (add-stop-fn!
                 (fn []
                   (cljs.core.async.close! ~'runner)
                   (~'done)))))))
