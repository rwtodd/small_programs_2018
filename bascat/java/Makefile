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
	${JAR} --create --file=mlib/org.rwtodd.cmd.bascat.jar \
      -e org.rwtodd.cmd.bascat.Cmd \
      -C mods/org.rwtodd.cmd.bascat . 

clean:
	rm -rf mods mlib
