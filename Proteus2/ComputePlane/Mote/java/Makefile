GEN=RadioSignalResultsMsg.java Constants.java SendBeaconMsg.java

all: RadioSignalMeter.jar

RadioSignalMeter.jar: RadioSignalMeter.class
	jar cf $@ *.class

RadioSignalResultsMsg.java: ../RadioSignalMeter.h
	mig -I.. -target=null -java-classname=RadioSignalResultsMsg java ../RadioSignalMeter.h RadioSignalResultsMsg -o $@

SendBeaconMsg.java: ../RadioSignalMeter.h
	mig -I.. -target=null -java-classname=SendBeaconMsg java ../RadioSignalMeter.h SendBeaconMsg -o $@

Constants.java: ../RadioSignalMeter.h
	ncg -target=null -java-classname=Constants java ../RadioSignalMeter.h TIMER_PERIOD_MILLI -o $@

RadioSignalMeter.class: $(wildcard *.java) $(GEN)
	javac *.java

clean:
	rm -f *.class $(GEN)

veryclean: clean
	rm -f RadioSignalMeter.jar
