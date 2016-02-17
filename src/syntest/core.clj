(ns syntest.core)

(defmacro syn-run! [& parts]
  `(do (cljs.test/async
        ~'done
        (let [~'is-done? (cljs.core.async/chan 1)]
          ~@(list (concat [`cljs.core.async.macros/go]
                              parts [`(cljs.core.async/put! ~'is-done? true) '(done)]))
          (cljs.core.async.macros/go
            (let [[~'val ~'port] (cljs.core.async/alts!
                                  [~'is-done?
                                   (cljs.core.async/timeout (deref ~`timeout-delay))])]
              (when-not (= ~'port ~'is-done?)
                (~'done))))))))
