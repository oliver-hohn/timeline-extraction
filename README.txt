[JK2] Automated Timeline Extraction, Oliver Philip H�hn

The following README will provide commands to run the program, and other general information.
The project requires Java 8 (or above), and a JDK 8 (or above).

Gradle(v3.3+) is installed:
	If you have Gradle installed, then proceed to this ROOT directory in command line or terminal.
	Then to retrieve all the libraries (that are not included), use the command: gradle build
	Note Internet access is required to retrieve the libraries.
	To then run the program use the command: gradle run
	This will launch the GUI where you can load documents, such as the ones provided in the "articles and timelines" subfolder.
	Note that for each article present, their relevant PDF and JSON timeline have been produced. This can be used to ensure that the produced timelines match.
	Note that for the produced timelines, a threshold value of 10 was used.
	To run the test, use the command: gradle test
	
If Gradle is not installed:
	If Gradle is not installed, the provided Gradle wrapper can be used.
	Proceed to this ROOT directory in command line or terminal.
	To build (i.e. retrieve the libraries and run tests), ensure internet access is available else it will fail:
		If on windows, use the command: gradlew build
		If on a UNIX system, use the command ./gradlew build
	To run the tests only:
		If on windows, use the command: gradlew test
		If on a UNIX system, use the command ./gradlew test
	To run the system (with the UI):
		If on windows, use the command: gradlew run
		If on a UNIX system, use the command ./gradlew run
		
		
Libraries used in the project:
	Stanford CoreNLP and the corresponding English models,
	Apache PDFBox,
	Apache POI,
	Apache Commons,
	RichTextFX,
	GSON,
	Joda Time,
	JUnit
	
Thanks goes out to the developers and providers of the libraries!
	