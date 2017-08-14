(ns syntest.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [syntest.core-test]))

(doo-tests 'syntest.core-test)
