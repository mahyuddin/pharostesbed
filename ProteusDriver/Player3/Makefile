all: libproteusdriver.so

proteus_driver.o: proteus_driver.cc proteus_comms.c proteus_comms.h
	g++ `pkg-config --cflags playercore` -c proteus_driver.cc proteus_comms.c `pkg-config --libs playercore`

libproteusdriver.so: proteus_driver.o proteus_comms.o
	g++ -shared -nostartfiles -o libproteusdriver.so proteus_driver.o proteus_comms.o

clean:
	rm -f *.o *.so
