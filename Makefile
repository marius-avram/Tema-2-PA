build: 
	javac *.java
run:
	java Main localhost 6666 5 5
clean:
	$(RM) *.class