.PHONY: all build dist clean

JAVAC="javac"
JAR="jar"

all: build dist

mods:
	mkdir -p mods

mlib:
	mkdir -p mlib

build: mods
	${JAVAC} -d mods --module-source-path=src $$(find src -name "*.java")

dist: mlib
	${JAR} --create --file=mlib/com.darktalents.cmd.bascat.jar \
      -e com.darktalents.cmd.bascat.Cmd \
      -C mods/com.darktalents.cmd.bascat . 

clean:
	rm -rf mods mlib
