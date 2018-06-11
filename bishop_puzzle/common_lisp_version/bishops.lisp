;; note-- load the file and then compile it to fasl, so
;; that the compiler knows about `winning-board`.  I'm sure
;; there is a way around this with `eval-when` but I haven't
;; tried to work around it.
(defconstant +rows+ 4)
(defconstant +cols+ 7)

(defun empty-board ()
  (make-array (* +rows+ +cols+)
	      :element-type '(unsigned-byte 2)
	      :initial-element 0))

(defun starting-board ()
  "create the starting board"
  (loop :with b = (empty-board)
	:for i :from 0 :below (array-dimension b 0) :by +cols+
	:do (setf (aref b i) 1
		  (aref b (+ i +cols+ -1)) 2)
	:finally (return b)))

(defun invert-board (b)
  (loop :for i :from 0 :below (array-dimension b 0)
	:when (> (aref b i) 0) :do (setf (aref b i) (- 3 (aref b i)))
	:finally (return b)))

(defun winning-board () (invert-board (starting-board)))

(defun hash-board (b)
  (declare (optimize (speed 3) (safety 0)) 
           (type (simple-array (unsigned-byte 2) *) b))
  (loop :for v :of-type (unsigned-byte 2) :across b
	:for tot :of-type (unsigned-byte 64) = v :then (logior v (the (unsigned-byte 64) (ash tot 2)))
	:finally (return tot)))

(defconstant +winning-hash+ (hash-board (winning-board)))

(defparameter *seen-boards* (make-hash-table :test #'eql))

(defun seen-p (b)
  "insert the board into the hash, and return t/nil if we had seen it,
   and t/nil if it was the winning board"
  (let* ((h (hash-board b))
	 (won (= h +winning-hash+))
	 (seen (gethash h *seen-boards*)))
    (unless seen (setf (gethash h *seen-boards*) t))
    (values seen won)))

(defun print-board (b)
  (loop :for i :from 0 :below +rows+ :do
	(loop :for j :from 0 :below +cols+ :do
              (let ((val (aref b (+ j (* i +cols+)))))
		(princ (cond ((= val 0) #\-)
                             ((= val 1) #\W)
                             ((= val 2) #\B)
                             (t         #\X)))))
	(format t "~%")))

(defun board-delta (i j)
  "determine the right value to add to i, which will take you
   diagonally to j"
  (declare (optimize (speed 3) (safety 0))
           (type fixnum i j))
  (let ((diff (- j i)))
    (* (signum diff) (+ +cols+
			(if (zerop (mod diff (+ +cols+ 1))) 1 -1)))))

(defun bishop-for-each (center action)
  "perform action for every space a bishop can reach from center"
  (declare (optimize (speed 3) (safety 0))
           (type (unsigned-byte 64) center)
           (type function action))
  (let* ((cx (mod center +cols+))
         (cy (floor center +cols+))
         (last-row (- +rows+ 1))
         (last-col (- +cols+ 1))
         (xc (- last-col cx))
         (yc (- last-row cy)))
    (declare (type (unsigned-byte 64) cx cy last-row last-col xc yc))
    (loop :for i :of-type (unsigned-byte 64) 
          :from (+ (max (- cx cy) 0) (* +cols+ (max (- cy cx) 0)))
          :to   (+ (min (+ cx yc) last-col) (* +cols+ (min (+ cy xc) last-row)))
          :by (+ +cols+ 1) 
          :when (not (= i center))
          :do (funcall action i))
    (loop :for i :of-type (unsigned-byte 64) 
          :from (+ (min (+ cx cy) last-col) (* +cols+ (max (- cy xc) 0)))
          :to   (+ (max (- cx yc) 0) (* +cols+ (min (+ cy cx) last-row)))
          :by (- +cols+ 1) 
          :when (not (= i center))
          :do (funcall action i))))

(defun attacked-spaces (b)
  "return a new board showing all attacked spaces"
  (declare (optimize (speed 3) (safety 0)) 
           (type (simple-array (unsigned-byte 2) *) b))
  (let ((answer (copy-seq b)))
    (loop :for i :from 0 :below (array-dimension b 0)
	  :as p = (aref b i) :when (> p 0)
	  :do (bishop-for-each i #'(lambda (j)
				     (setf (aref answer j)
					   (logior (aref answer j) p))))
	  :finally (return answer))))

(defstruct bmove 
  (board  nil :type (simple-array (unsigned-byte 2) *)  :read-only t)
  (parent nil :read-only t)
  (won    nil :type boolean :read-only t)
  (move   nil :type list :read-only t)
  )

(defun push-next-moves (m result)
  "generate the moves that could follow m, and push them onto result"
  (declare (optimize (speed 3) (safety 0))
           (type bmove m)
           (type (array bmove) result))
  (let* ((b (bmove-board m))
         (attacked (attacked-spaces b)))
    (flet ((clear-path (p i j)
		       (and
			;; the final space is not attacked...
			(zerop (logand (- 3 p) (aref attacked j)))
			;; all the spaces in the move are empty...
			(loop :with delta = (board-delta j i)
			      :for x :of-type fixnum = j :then (+ x delta)
			      :until (= x i) :always (zerop (aref b x))))))
      (loop :for i :of-type fixnum :from 0 :below (array-dimension b 0)
	    :as p = (aref b i) :when (plusp p)
	    :do (bishop-for-each i
				 #'(lambda (j)
				     (when (clear-path p i j)
				       (let ((nb (copy-seq b)))
					 (setf (aref nb j) p
					       (aref nb i) 0)
					 (multiple-value-bind (seen won) (seen-p nb)
					   (unless seen
					     (vector-push-extend (make-bmove :board nb :parent m :won won :move (list i j))
								 result)))))))))))

(defun print-bmoves (m)
  "display board moves"
  (flet ((square (n)
		 (format nil "~c~d"
			 (aref "ABCDEFGHI" (mod n +cols+))
			 (+ 1 (floor n +cols+)))))
    (loop :for x = m :then (bmove-parent x) :until (null x)
	  :do (print-board (bmove-board x))
	  (if (bmove-move x)
	      (format t "~%~s to ~s~2%"
		      (square (first (bmove-move x)))
		      (square (second (bmove-move x))))))))

(defun run-game ()
  (loop :for moves = 0 :then (+ moves 1)
        :for backlog = (vector (make-bmove :board (starting-board)
					   :parent nil
					   :won nil
					   :move nil))
	:then (let ((next-moves (make-array 0 :adjustable t :fill-pointer 0)))
		(loop :for m :across backlog :do (push-next-moves m next-moves))
		next-moves)
	:while (> (length backlog) 0)
	:do (format t "Move ~d: Moves to consider: ~d~%"
		    moves
		    (length backlog)) 
	:until (let ((winner (find t backlog :key #'bmove-won)))
		 (if winner
		     (print-bmoves winner))
		 winner)))
