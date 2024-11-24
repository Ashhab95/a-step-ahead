@echo off
setlocal enabledelayedexpansion

if exist "release" (
    rmdir /s /q "release"
)


echo compiling

javac --module-path lib/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.graphics,javafx.swing -cp .;release/;lib/javacv-platform-1.5.9-bin/*;resources/img;resources/img;resources/xml;resources/models -d release src/main/java/org/fys/utils/Token.java



javac --module-path lib/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.graphics,javafx.swing -cp .;release/;lib/javacv-platform-1.5.9-bin/*;resources/img;resources/img;resources/xml;resources/models -d release src/main/java/org/fys/controller/ConcurrencyManager.java

javac --module-path lib/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.graphics,javafx.swing -cp .;release/;lib/javax/*;lib/javacv-platform-1.5.9-bin/*;resources/img;resources/img;resources/xml;resources/models; -d release src/main/java/org/fys/view/*.java
javac --module-path lib/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.graphics,javafx.swing -cp .;release/;lib/javacv-platform-1.5.9-bin/*;lib/gson/gson-2.10.1.jar;resources/img;resources/img;resources/xml;resources/models; -d release src/main/java/org/fys/models/*.java

javac --module-path lib/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.graphics,javafx.swing -cp .;release/;lib/javacv-platform-1.5.9-bin/*;resources/img;resources/img;resources/xml;resources/models -d release src/main/java/org/fys/controller/AppController.java

javac --module-path lib/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.graphics,javafx.swing -cp .;release/;lib/javacv-platform-1.5.9-bin/*;resources/img;resources/img;resources/xml;resources/models -d release src/main/java/org/fys/*.java





set sourceDirectory=lib
set destinationDirectory=release/lib

xcopy "%sourceDirectory%" "%destinationDirectory%" /E /I /H /Y

set si=resources
set dsti=release/resources

xcopy "%si%" "%dsti%" /E /I /H /Y

echo executing

cd release
mkdir out
mkdir out\model
mkdir out\img

mkdir python
cd ..
set sourceDirectory=src\main\python\
set destinationDirectory=release\python\

:: Copy the file from source to destination
xcopy %sourceDirectory% %destinationDirectory% /E /I /H /Y

:: Check if the copy was successful
if %errorlevel% == 0 (
    echo File copied successfully.
) else (
    echo Error copying file.
)

cd release
java  -cp .;lib/gson/gson-2.10.1.jar;lib/javacv-platform-1.5.9-bin/*;resources/img;resources/xml;resources/models  --module-path lib/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.graphics  org.fys.main



::javac --module-path jmonkey --add-modules core -cp .; Test3DModel.java

::>javac -cp "jmonkey\jme3-core-3.7.0-stable.jar;jme3-lwjgl3-3.7.0-stable.jar;." Test3DModel.java
::java -cp "jmonkey\*.jar;jmonkey\jme3-core-3.7.0-stable.jar;jmonkey\jme3-lwjgl3-3.7.0-stable.jar;." Test3DModel





::jme3-core-3.7.0-stable






::Djava.library.path=lib\javacpp-platform-1.5.9-bin\x64



:: javac -cp .;lib\opencv-490\opencv-490.jar hello.java
:: java  --module-path lib/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.fxml -cp lib\opencv-490\opencv-490.jar -Djava.library.path=lib\opencv-490\x64 hello

:: javac --module-path lib/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.graphics -d target/src/main/java src/main/java/ui/components/*.java

::java --module-path lib/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.fxml -cp bin com.yourpackage.YourMainClass

pause