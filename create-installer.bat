call mvn package
jpackage --input .\target\ --main-jar mtg-workstation-%1.jar --name mtg-workstation --win-dir-chooser --win-menu --win-shortcut --icon src\main\resources\icon\icon.ico --app-version %1
