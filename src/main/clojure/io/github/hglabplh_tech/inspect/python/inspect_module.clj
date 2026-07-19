(ns io.github.hglabplh-tech.inspect.python.inspect-module
  (:import (io.github.hglabplh_tech.python.inspect.api.utils InspectPythonCode)))

(defn inspect-module
  "inspect a python module"
  {:added  "1.4"
   :static true}
  [mod_name, pycfile_path]
  (let [inspectPy (InspectPythonCode. pycfile_path)
      result (.inspectModule inspectPy mod_name pycfile_path)]
    (print result)
    ))


