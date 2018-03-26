md2: md2.o
	gcc -o md2 md2.o  -llua `sdl2-config --libs` -lm -ldl

md2.o: md2.c
	gcc `sdl2-config --cflags` -march=native -O3 -c md2.c 

.PHONY: clean
clean:
	-rm md2.o md2
