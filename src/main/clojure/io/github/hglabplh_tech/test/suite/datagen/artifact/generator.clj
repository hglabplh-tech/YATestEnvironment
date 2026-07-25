(ns io.github.hglabplh_tech.test.suite.datagen.artifact.generator
  (:require [clojure.string :as str]
            [io.github.hglabplh_tech.test.suite.datagen.artifact.domain :as domain])
  (:import (java.awt Color Font)
           (java.awt.image BufferedImage)
           (java.io ByteArrayOutputStream)
           (java.nio.charset StandardCharsets)
           (java.util Random UUID)
           (java.util.zip ZipEntry ZipOutputStream)
           (javax.imageio ImageIO)))

(def first-names ["Ada" "Grace" "Edsger" "Barbara" "Donald" "Margaret"])
(def last-names ["Lovelace" "Hopper" "Dijkstra" "Liskov" "Knuth" "Hamilton"])

(defn- utf8-bytes [value]
  (.getBytes (str value) StandardCharsets/UTF_8))

(defn- sample-text [artifact]
  (let [hierarchy (:hierarchy artifact)]
    (str "Synthetic test data\n"
         "Format: " (name (:format artifact)) "\n"
         "Path: " (str/join "/" (domain/hierarchy-segments hierarchy)) "\n"
         "Sequence: " (:sequence-no artifact) "\n"
         "Artifact: " (:id artifact) "\n")))

(defn- escape-pdf-text [value]
  (-> value
      (str/replace "\\" "\\\\")
      (str/replace "(" "\\(")
      (str/replace ")" "\\)")
      (str/replace #"\r?\n" " / ")))

(defn- pdf-bytes [artifact]
  (let [stream (str "BT /F1 12 Tf 72 720 Td (" (escape-pdf-text (sample-text artifact)) ") Tj ET")
        stream-bytes (utf8-bytes stream)]
    (utf8-bytes
     (str "%PDF-1.4\n"
          "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n"
          "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n"
          "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
          "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n"
          "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n"
          "5 0 obj << /Length " (alength stream-bytes) " >> stream\n"
          stream
          "\nendstream endobj\n"
          "trailer << /Root 1 0 R >>\n%%EOF\n"))))

(defn- xml-escape [value]
  (-> (str value)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")
      (str/replace "\"" "&quot;")
      (str/replace "'" "&apos;")))

(defn- zip-entry! [^ZipOutputStream zip name content]
  (.putNextEntry zip (ZipEntry. name))
  (.write zip (utf8-bytes content))
  (.closeEntry zip))

(defn- docx-bytes [artifact]
  (let [out (ByteArrayOutputStream.)
        text (xml-escape (sample-text artifact))]
    (with-open [zip (ZipOutputStream. out)]
      (zip-entry! zip "[Content_Types].xml"
                  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>")
      (zip-entry! zip "_rels/.rels"
                  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>")
      (zip-entry! zip "word/document.xml"
                  (str "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body><w:p><w:r><w:t xml:space=\"preserve\">"
                       text
                       "</w:t></w:r></w:p></w:body></w:document>")))
    (.toByteArray out)))

(defn- image-bytes [artifact image-format]
  (let [image (BufferedImage. 640 360 BufferedImage/TYPE_INT_RGB)
        graphics (.createGraphics image)
        out (ByteArrayOutputStream.)]
    (try
      (.setColor graphics Color/WHITE)
      (.fillRect graphics 0 0 640 360)
      (.setColor graphics (Color. 30 90 120))
      (.setFont graphics (Font. "SansSerif" Font/BOLD 28))
      (.drawString graphics "YATestEnvironment" 40 80)
      (.setFont graphics (Font. "SansSerif" Font/PLAIN 18))
      (.drawString graphics (str "Format: " (name (:format artifact))) 40 130)
      (.drawString graphics (str "Sequence: " (:sequence-no artifact)) 40 160)
      (.drawString graphics (str "Artifact: " (:id artifact)) 40 190)
      (when-not (ImageIO/write image image-format out)
        (throw (ex-info "No ImageIO writer is available for artifact image format."
                        {:image-format image-format
                         :format (:format artifact)})))
      (.toByteArray out)
      (finally
        (.dispose graphics)))))

(defn- relational-row [artifact ^Random rng]
  (let [first-name (nth first-names (.nextInt rng (count first-names)))
        last-name (nth last-names (.nextInt rng (count last-names)))
        amount (/ (.nextInt rng 100000) 100.0)]
    {:id (:id artifact)
     :external-id (str (UUID/randomUUID))
     :first-name first-name
     :last-name last-name
     :email (str (str/lower-case first-name) "." (str/lower-case last-name) "@example.test")
     :amount amount
     :active? (.nextBoolean rng)
     :created-at (:created-at artifact)
     :sequence-no (:sequence-no artifact)}))

(defmulti generate-content
  (fn [artifact _rng] (:format artifact)))

(defmethod generate-content :txt [artifact _rng]
  {:bytes (utf8-bytes (sample-text artifact))})

(defmethod generate-content :pdf [artifact _rng]
  {:bytes (pdf-bytes artifact)})

(defmethod generate-content :docx [artifact _rng]
  {:bytes (docx-bytes artifact)})

(defmethod generate-content :jpeg [artifact _rng]
  {:bytes (image-bytes artifact "jpeg")})

(defmethod generate-content :tiff [artifact _rng]
  {:bytes (image-bytes artifact "tiff")})

(defmethod generate-content :relational [artifact rng]
  {:row (relational-row artifact rng)})

(defmethod generate-content :default [artifact _rng]
  (throw (ex-info "Unsupported artifact format."
                  {:format (:format artifact)
                   :supported domain/data-formats})))

(defn generate-artifact
  ([job sequence-no]
   (generate-artifact job sequence-no (:seed job 42)))
  ([job sequence-no seed]
   (let [artifact (domain/base-artifact job sequence-no)
         rng (Random. (+ (long seed) (long sequence-no)))]
     (merge artifact (generate-content artifact rng)))))
