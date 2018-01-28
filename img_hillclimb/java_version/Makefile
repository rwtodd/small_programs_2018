.PHONY: all build dist clean

JAVAC=javac
JAR=jar
MPATH=${HOME}/Documents/jmod

all: build dist

mods:
	mkdir -p mods

mlib:
	mkdir -p mlib

build: mods
	${JAVAC} -d mods --module-path=${MPATH} --module-source-path=src $$(find src -name "*.java")

dist: mlib
	${JAR} --create --file=mlib/com.darktalents.cmd.asciipic.jar \
      -e com.darktalents.cmd.asciipic.Cmd \
      -C mods/com.darktalents.cmd.asciipic . 

clean:
	rm -rf mods mlib
