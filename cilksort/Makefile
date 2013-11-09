
# enable ICC with this:
# module add intel

ifeq ($(CC),gcc)
  CC=gcc
  LDFLAGS=-lm 
else
  CC=icc
  LDFLAGS=-lm -lcilkrts -lirc -limf
endif

CFLAGS= -Wall -O3

cilksort: cilksort.o
	$(CC) $(CFLAGS) $^ -o $@

cilksort-nomain.o: cilksort.c
	$(CC) $(CFLAGS) -c $^ -o $@

cilksort.o: cilksort.c
	$(CC) $(CFLAGS) -DUSE_MAIN=1 -c $^ -o $@

run:
	./cilksort $(RUN_ARGS)

clean:
	rm -f cilksort *.o

.PHONY: clean

