# --add-opens args used to open modules and allow illegal(reflection\private classes and fields) access from the code.
java $JAVA_OPTS --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED -jar /app.jar
