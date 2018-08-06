mkdir mods
mkdir mlib

# to build
javac -d mods --module-source-path=src (Get-ChildItem . -Recurse -File -Include '*.java')

# generate jar
jar --create --file=mlib/org.rwtodd.cmd.bascat.jar -e org.rwtodd.cmd.bascat.Cmd -C mods/org.rwtodd.cmd.bascat .

