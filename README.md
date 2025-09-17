# [![logo](https://www.jcabi.com/logo-square.svg)](https://www.jcabi.com/logo-square.svg)

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](https://www.rultor.com/b/jcabi/jcabi-aspects)](https://www.rultor.com/p/jcabi/jcabi-aspects)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/jcabi/jcabi-aspects/actions/workflows/mvn.yml/badge.svg)](https://github.com/jcabi/jcabi-aspects/actions/workflows/mvn.yml)
[![PDD status](https://www.0pdd.com/svg?name=jcabi/jcabi-aspects)](https://www.0pdd.com/p?name=jcabi/jcabi-aspects)
[![Javadoc](https://javadoc.io/badge/com.jcabi/jcabi-aspects.svg)](https://www.javadoc.io/doc/com.jcabi/jcabi-aspects)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-aspects/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-aspects)
[![codecov](https://codecov.io/gh/jcabi/jcabi-aspects/branch/master/graph/badge.svg)](https://codecov.io/gh/jcabi/jcabi-aspects)

More details are here: [aspects.jcabi.com](https://aspects.jcabi.com/index.html)

Also, read this blog post: [Java Method Logging with AOP and Annotations](https://www.yegor256.com/2014/06/01/aop-aspectj-java-method-logging.html).

This module contains a collection of useful [AOP](https://en.wikipedia.org/wiki/Aspect-oriented_programming)
aspects, which
allow you to modify the behavior of a Java application without
writing a line of code. For example, you may want to retry HTTP
resource downloading in case of failure. You can implement a full
`do/while` cycle yourself, or you can annotate your method with
`@RetryOnFailure` and let one of our AOP aspects do the work for you:

```java
import com.jcabi.aspects.RetryOnFailure;

public class MyResource {
    @RetryOnFailure
    public String load(final URL url) {
        return url.openConnection().getContent();
    }
}
```

Full list of AOP annotations is [here](https://aspects.jcabi.com/).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```bash
mvn clean install -Pqulice
```
