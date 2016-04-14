JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		Game.java \
		Player.java \
		Tir.java \
		House.java \
		Alien.java \
		Motor.java \
		Movit.java \
		Bonus.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class
