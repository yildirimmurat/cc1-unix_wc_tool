Create executable jar file
```
javac UnixWC.java
jar cfe UnixWC.jar UnixWC UnixWC.class
```

Create a ccwc binary file in a 'path' location
```
cd /path/to/binary/directory
sudo touch ccwc
sudo chmod +x ccwc
sudo vi ccwc
```

Add shebang line at the beginning of ccwc script
```
#!/bin/bash
java -jar /path/to/UnixWC.jar "$@"
```

Usage:
```
ccwc test.txt
```
