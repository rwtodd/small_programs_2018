;; define a set of keymap controls that gives emacs
;; modal methods of issuing commands.

;; ------------------------------------------------------------------------
;; TODO:
;;  - decide whether to fix it so U's stack up
;; ------------------------------------------------------------------------

(require 'seq) ; sequence functions like seq-filter

(defvar modal-cmd-arg nil "arg so far")
(defvar modal-cmd-so-far nil "keymaps in play so far for a command")

(defun modal-exit ()
  "When g pressed"
  (interactive)
  (setq modal-cmd-arg nil)
  (setq modal-cmd-so-far nil)
  (command-execute 'keyboard-quit))

(defvar modal-keymaps nil
  "a list of the global and local keymaps in effect")

(defvar modal-special-chars nil
   "an assoc-list of special cases that don't fit the `modal-adjust-key` scheme")

(defun modal-binding (key &optional maps)
  (let* ((keymaps (or maps modal-keymaps))
	 (result (some (lambda (keymap) (lookup-key keymap key t))
		      keymaps)))
    (if (keymapp result)
	;; if we found a keymap... pull up ALL the keymaps we can find
	(seq-filter #'keymapp
		    (mapcar (lambda (keymap) (lookup-key keymap key t)) keymaps))
      result)))

(defun modal-adjust-key (k)
  "adjust the modality (control,meta,shift)"
  (let ((mods (event-modifiers k)))
    (cond
     ((null mods) (event-apply-modifier k 'control 26 "C-"))
     ((member 'control mods)
      (let ((ebt (event-basic-type k)))
	(when (member 'shift mods)
	  (setq ebt (event-apply-modifier ebt 'shift 25 "S-")))
	(setq last-command-event ebt) ; in case we run #'self-insert-command
	ebt))
     ((member 'shift mods) (event-apply-modifier (event-basic-type k)
						 'meta 27 "M-"))
     (t k))))

(defun modal-self-insert (n)
  "insert a modal command"
  (interactive "P")
  (let* ((arg (or modal-cmd-arg n))
	 (key (aref (this-command-keys) 0))
	 (cmd (or (alist-get key modal-special-chars)
		  (modal-binding (vector (modal-adjust-key key))
				 (or modal-cmd-so-far modal-keymaps)))))
    (cond
     ((null cmd)
      (setq modal-cmd-arg nil)
      (setq modal-cmd-so-far nil))
     ((consp cmd)
      (setq modal-cmd-arg arg)
      (setq modal-cmd-so-far cmd)
      nil)
     ((commandp cmd)
      (setq modal-cmd-arg nil)
      (setq modal-cmd-so-far nil)
      (setq prefix-arg arg)
      (command-execute cmd))
     (t nil))))

(defun modal-collect-keymaps ()
  "get all the active minor mode maps, and attatch them to the local and global maps"
  (append
   (mapcan (lambda (mm)
	     (let ((name (car mm)))
	       (and (eval name)
		    (not (eq name 'modal-entry-mode))
		    (list (cdr mm)))))
	   minor-mode-map-alist)
   (seq-filter #'keymapp (list (current-local-map) (current-global-map)))))

(defun modal-reset-keymaps ()
  "if the keymaps we collected before aren't correct, rest them"
  (interactive)
  (set (make-local-variable 'modal-keymaps)
       (modal-collect-keymaps))
  (set (make-local-variable 'modal-special-chars)
       (list (cons (aref (kbd "<escape>") 0) (list esc-map))
	     (cons (aref (kbd "m") 0) (list esc-map))
	     (cons (aref (kbd "<") 0) (modal-binding (kbd "M-<")))
	     (cons (aref (kbd ">") 0) (modal-binding (kbd "M->")))
	     (cons (aref (kbd "<backspace>") 0) (modal-binding (kbd "<DEL>"))))))

(define-minor-mode modal-entry-mode "modal entry minor mode"
  :init-value nil
  :lighter " modal"
  :keymap (let ((map (make-sparse-keymap)))
	    (define-key map "g" #'modal-exit)
	    (define-key map [t] #'modal-self-insert)
	    map)
  (when (and modal-entry-mode (null modal-special-chars))
    (modal-reset-keymaps)))

(global-set-key (kbd "C-,") #'modal-entry-mode)

(provide  'rwt-modal)
