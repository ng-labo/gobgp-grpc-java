### usage

- copy from *.proto in gobgp/api into src/main/proto directory
- `mvn package` then java-grpc files and executable jar will be generated in target directory.
- `java -jar target/grpc-gobgp-1.0-SNAPSHOT-jar-with-dependencies.jar` then a example will run.

### using leiningen

- Now instead of maven, I start to use [leiningen](https://leiningen.org/) as build tool.
- `lein uberjar` then being run protoc, javac and uberjar.      
- `java -jar target/uberjar/gobgp-grpc-java-0.1.0-SNAPSHOT-standalone.jar` then a example will run.
- `lein repl` can let me feel like running interpreter, strictly not.

```Clojure
gobgp-grpc-java.core=> (gobgpapi.example.Client/process "add")
call addPath
Done
nil
gobgp-grpc-java.core=> (gobgpapi.example.Client/process "del")
call delPath
Done
nil
```

### using sbt

- `sbt assembly`
