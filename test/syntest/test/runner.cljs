(ns syntest.test.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [cljs.test :as test]
            [syntest.test.core]))

(doo-tests 'syntest.test.core)
