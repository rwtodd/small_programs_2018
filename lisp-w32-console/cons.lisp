;; just playing with cffi

(defpackage "RT-CONSPLAY"
  (:use :cl :cffi)
  (:export #:random-chars))

(in-package "RT-CONSPLAY")

(defcstruct small-rect
    "rectangle shorts"
  (left :short)
  (top :short)
  (right :short)
  (bottom :short))

(defcstruct coord
    "coordinate"
  (x :short)
  (y :short))

(defcstruct buffer-info
    "console screen buffer info"
  (dwSize (:struct coord))
  (dwCursorPosition (:struct coord))
  (wAttributes :unsigned-short)
  (srWindow (:struct small-rect))
  (dwMaximumWindowSize (:struct coord)))

(define-foreign-library kernel32 (t (:default "kernel32")))
(use-foreign-library kernel32)

(defun get-stdout () (foreign-funcall "GetStdHandle" :long -11 :pointer))

(defun lookup-scr-size (handle)
  (with-foreign-object (info '(:struct buffer-info))
    (foreign-funcall "GetConsoleScreenBufferInfo"
		     :pointer handle
		     :pointer info
		     :int)
    (let ((srw (foreign-slot-pointer info '(:struct buffer-info) 'srWindow)))
      (with-foreign-slots ((left top right bottom) srw (:struct small-rect))
        (values left right top bottom)))))

(defun set-cursor-pos (handle x y)
  (foreign-funcall "SetConsoleCursorPosition"
		     :pointer handle
		     :int (logior (ash y 16) x)
		     :int))
 
(defun random-chars ()
  (let ((handle (get-stdout)))
    (multiple-value-bind (left right top bottom) (lookup-scr-size handle)
      (let ((width (- right left))
            (height (- bottom top)))
        (loop :for idx :from 1 :to (* 2 width height)
            :and x = (+ left (random (1+ width)))
            :and y = (+ top (random (1+ height)))
            :and chr = (code-char (+ 33 (random 94)))
          :do (set-cursor-pos handle x y)
              (write-char chr)
              (force-output)))))) 

