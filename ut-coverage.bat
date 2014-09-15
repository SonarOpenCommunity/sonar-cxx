call mvn org.jacoco:jacoco-maven-plugin:prepare-agent clean install
call mvn org.jacoco:jacoco-maven-plugin:prepare-agent clean install -Pcoverage-per-test
call mvn sonar:sonar